package com.elltor.oplog.aspect;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.core.LogRecordContext;
import com.elltor.oplog.core.LogRecordEvaluationContext;
import com.elltor.oplog.core.LogRecordValueParser;
import com.elltor.oplog.entity.LogRecordOps;
import com.elltor.oplog.entity.Operator;
import com.elltor.oplog.entity.ProceedResult;
import com.elltor.oplog.entity.Record;
import com.elltor.oplog.factory.LogRecordOperationFactory;
import com.elltor.oplog.factory.ParseFunctionFactory;
import com.elltor.oplog.service.ILogRecordService;
import com.elltor.oplog.service.IOperatorGetService;
import com.elltor.oplog.service.IParseFunction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志处理切面
 */

@Aspect
@Component
public class LogRecordAspect {

    /**/
    @Resource
    private LogRecordOperationFactory logRecordOperationFactory;

    /**
     * 持久化日志日志
     */
    @Resource
    private ILogRecordService logRecordService;

    /**
     * 模板解析
     */
    @Resource
    private LogRecordValueParser parser;

    /**
     * 解析函数工具方法
     */
    @Resource
    private ParseFunctionFactory fact;

    @Resource
    private IOperatorGetService operatorGetService;

    /**
     * 缓存日志注解
     */
    private ConcurrentHashMap<AnnotatedElementKey, LogRecord> targetAnnotationCache = new ConcurrentHashMap<>();

    private ParameterNameDiscoverer parameterNameDiscover = new DefaultParameterNameDiscoverer();

    private Logger log = LoggerFactory.getLogger(LogRecordAspect.class);

    // 注解切入
    @Pointcut("@annotation(com.elltor.oplog.annotation.LogRecord)")
    public void pointCut() { /*仅作为切面调用标记*/}

    @Around(value = "pointCut()")
    public Object logRecordAround(ProceedingJoinPoint point) throws Throwable {
        return processRecordLog(point);
    }

    /**
     * 处理切面日志
     */
    private Object processRecordLog(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        Method targetMethod = ((MethodSignature) point.getSignature()).getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        AnnotatedElementKey targetMethodKey = new AnnotatedElementKey(targetMethod, targetClass);
        LogRecord logRecord = getTargetAnnotationOrUpdateCache(targetMethodKey, point);
        Object ret = null;

        if (logRecord == null) {
            return null;
        }

        ProceedResult proceedResult = new ProceedResult(true, null, "OK");

        // 执行方法前入栈空map
        LogRecordContext.putEmptySpan();

        // 解析的LogRecord的属性
        List<LogRecordOps> operations;
        Map<String, String> functionNameAndReturnMap;
        operations = logRecordOperationFactory.computeLogRecordOperations(logRecord);
        // 过滤提前执行的函数
        Set<String> beforeExecuteFunctionNames = filterOperationFunctionNames(true, operations);
        // 业务逻辑执行前的自定义函数 解析
        try {
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(beforeExecuteFunctionNames,
                    operations, targetMethod, args, targetMethodKey);
        } catch (Exception e) {
            throw new Exception(proceedResult.getErrMsg());
        }
        try {
            ret = point.proceed();
        } catch (Throwable e) {
            proceedResult = new ProceedResult(false, e, e.getMessage());
        }
        try {
            // 持久化记录日志
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(ret, targetMethod, args, operations, proceedResult.isSuccess(),
                        proceedResult.getErrMsg(), functionNameAndReturnMap, targetMethodKey);
            }
        } catch (Exception t) {
            proceedResult = new ProceedResult(false, t, t.getMessage());
            // 记录日志错误
            log.error("log record parse exception", t);
        } finally {
            LogRecordContext.clear();
        }
        if (proceedResult.getThrowable() != null) {
            throw proceedResult.getThrowable();
        }
        return ret;
    }

    /**
     * 处理提前执行的函数
     *
     * @param beforeExecuteFunctions 提前执行函数集合
     * @param operations             模板操作集合
     * @param targetMethod           目标方法
     * @param args                   目标方法入参
     * @param targetMethodKey        目标方法标识key
     * @return 处理的模板操作的key与处理后模板的映射map
     */
    private Map<String, String> processBeforeExecuteFunctionTemplate(Set<String> beforeExecuteFunctions,
                                                                     List<LogRecordOps> operations,
                                                                     Method targetMethod,
                                                                     Object[] args,
                                                                     AnnotatedElementKey targetMethodKey) {

        if (beforeExecuteFunctions == null && beforeExecuteFunctions.isEmpty()) {
            return new HashMap<>(0);
        }

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                targetMethod, args, parameterNameDiscover, null, null);

        registerSpELFunction(beforeExecuteFunctions, logRecordEvaluationContext);

        // 注册自定义函数
        parser.setLogRecordEvaluationContext(logRecordEvaluationContext);
        Map<String, String> executeTemplateAndReturnMap = new HashMap<>();

        for (LogRecordOps op : operations) {
            if (!op.isTemplate()) {
                continue;
            }
            List<String> functionNames = op.getFunctionNames();
            if (functionNames.size() > 0 && beforeExecuteFunctions.containsAll(functionNames)) {
                String newTemplate = parser.parseExpression(op.getValue(), targetMethodKey);
                executeTemplateAndReturnMap.put(op.getKey(), newTemplate);
            }
        }
        return executeTemplateAndReturnMap;
    }

    /**
     * 持久化存储操作日志
     *
     * @param ret                方法返回值
     * @param targetMethod       目标方法
     * @param args               目标方法入参
     * @param operations         目标操作集合
     * @param complete           是否成功
     * @param errMsg             错误信息
     * @param executeTemplateMap 提前执行的模板
     * @param targetMethodKey    目标方法标识key
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void recordExecute(Object ret, Method targetMethod,
                               Object[] args, List<LogRecordOps> operations,
                               boolean complete,
                               String errMsg, Map<String, String> executeTemplateMap,
                               AnnotatedElementKey targetMethodKey) throws NoSuchFieldException, IllegalAccessException {

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                targetMethod, args, parameterNameDiscover, ret, errMsg);

        // 注册自定义函数
        Set<String> functionNames = filterOperationFunctionNames(false, operations);
        registerSpELFunction(functionNames, logRecordEvaluationContext);
        parser.setLogRecordEvaluationContext(logRecordEvaluationContext);

        // 持久化日志POJO
        Record record = new Record();
        Class<Record> clazz = Record.class;

        for (LogRecordOps op : operations) {
            String key = op.getKey();
            String value = "";
            String oldTemplate = executeTemplateMap.get(key);
            if (op.isTemplate()) {
                if (functionNames.containsAll(op.getFunctionNames())) {
                    if (oldTemplate != null) {
                        value = parser.parseExpression(oldTemplate, targetMethodKey);
                    } else {
                        value = parser.parseExpression(op.getValue(), targetMethodKey);
                    }
                }
            } else {
                value = op.getValue();
            }
            final Field declaredField = clazz.getDeclaredField(key);
            declaredField.setAccessible(true);
            declaredField.set(record, value);
        }

        String operator = record.getOperator();
        // 操作用户
        if(operator == null || operator.isEmpty()){
            Operator user = operatorGetService.getUser();
            if(user != null){
                record.setOperator(user.getUsername());
            }
        }

        record.setComplete(complete);
        logRecordService.record(record);
    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        log.error("切面处理过程中出错, 错误原因: {}", e.getMessage());
    }

    private Set<String> filterOperationFunctionNames(boolean isBeforeExecute,
                                                      List<LogRecordOps> operations) {

        Set<String> filterFuncNames = new HashSet<>(operations.size() * 2);
        if (isBeforeExecute) {
            for (LogRecordOps op : operations) {
                if (!op.isTemplate()) {
                    continue;
                }
                for (String method : op.getFunctionNames()) {
                    if (fact.isBeforeFunction(method)) {
                        filterFuncNames.add(method);
                    }
                }
            }
        } else {
            for (LogRecordOps op : operations) {
                filterFuncNames.addAll(op.getFunctionNames());
            }
        }
        return filterFuncNames;
    }

    private void registerSpELFunction(Set<String> registerFunctions,
                                      LogRecordEvaluationContext ctx) {

        Iterator<String> it = registerFunctions.iterator();
        while (it.hasNext()) {
            String fn = it.next();
            IParseFunction function = fact.getFunction(fn);
            if (function == null) {
                it.remove();
                log.error("在向SpEL Context中注册自定义函数出错, 函数不存在, 函数名: {}", fn);
                continue;
            }
            ctx.registerFunction(fn, function.functionMethod());
        }
    }

    private LogRecord getTargetAnnotationOrUpdateCache(AnnotatedElementKey cacheKey,
                                                       ProceedingJoinPoint point) {

        LogRecord targetAnnotation = targetAnnotationCache.get(cacheKey);
        if (targetAnnotation != null) {
            return targetAnnotation;
        } else {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            LogRecord logRecord = targetMethod.getAnnotation(LogRecord.class);
            targetAnnotationCache.put(cacheKey, logRecord);
            return logRecord;
        }
    }

}
