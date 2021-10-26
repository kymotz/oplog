package com.elltor.oplog.aspect;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.core.LogRecordContext;
import com.elltor.oplog.core.LogRecordEvaluationContext;
import com.elltor.oplog.core.LogRecordValueParser;
import com.elltor.oplog.entity.*;
import com.elltor.oplog.factory.LogRecordOperationFactory;
import com.elltor.oplog.factory.ParseFunctionFactory;
import com.elltor.oplog.publisher.LogRecordPublisher;
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
import org.springframework.stereotype.Component;

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

    @Resource
    private LogRecordOperationFactory logRecordOperationFactory;

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

    @Resource
    private LogRecordPublisher logRecordPublisher;

    /**
     * 缓存日志注解
     */
    private ConcurrentHashMap<AnnotatedElementKey, LogRecord> targetAnnotationCache = new ConcurrentHashMap<>();

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
        Method targetMethod = ((MethodSignature) point.getSignature()).getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        AnnotatedElementKey targetMethodKey = new AnnotatedElementKey(targetMethod, targetClass);
        LogRecord logRecord = getTargetAnnotationOrUpdateCache(targetMethodKey, point);

        SimpleLogRecordContext simpleContext = new SimpleLogRecordContext();
        simpleContext.setArgs(point.getArgs());
        simpleContext.setTargetMethod(targetMethod);
        simpleContext.setMethodKey(targetMethodKey);
        simpleContext.setLogRecord(logRecord);

        Object ret = null;

        if (logRecord == null) {
            return null;
        }

        ProceedResult proceedResult = new ProceedResult(true, null, "");

        // LogRecord Context
        LogRecordEvaluationContext ctx = new LogRecordEvaluationContext(simpleContext);

        // 解析的LogRecord的属性
        List<LogRecordOperation> operations;
        Map<String, String> functionNameAndReturnMap = Collections.emptyMap();
        operations = logRecordOperationFactory.computeLogRecordOperations(logRecord);
        // 过滤提前执行的函数
        Set<String> beforeExecuteFunctionNames = filterOperationFunctionNames(true, operations);
        // 业务逻辑执行前的自定义函数 解析
        try {
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(beforeExecuteFunctionNames,
                    operations, targetMethodKey, ctx);
        } catch (Exception e) {
            proceedResult = new ProceedResult(false, e, "前置函数执行出错");
        }

        // 执行方法前入栈空map
        LogRecordContext.putEmptySpan();

        try {
            ret = point.proceed();
        } catch (Throwable e) {
            proceedResult = new ProceedResult(false, e, "注解标注的方法执行出错");
        }
        try {

            simpleContext.setRet(ret);
            simpleContext.setErrMsg(proceedResult.getErrMsg());

            // 持久化记录日志
            recordExecute(simpleContext, operations, proceedResult.isSuccess(),
                    functionNameAndReturnMap, targetMethodKey, ctx);

        } catch (Exception t) {
            proceedResult = new ProceedResult(false, t, "持久化存储日志方法执行出错");
        } finally {
            LogRecordContext.clear();
        }
        if (proceedResult.getThrowable() != null) {
            // 记录日志错误
            log.error(proceedResult.getErrMsg());
            throw proceedResult.getThrowable();
        }
        return ret;
    }

    /**
     * 处理提前执行的函数
     *
     * @param beforeExecuteFunctions 提前执行函数集合
     * @param operations             模板操作集合
     * @param targetMethodKey        目标方法标识key
     * @return 处理的模板操作的key与处理后模板的映射map
     */
    private Map<String, String> processBeforeExecuteFunctionTemplate(Set<String> beforeExecuteFunctions,
                                                                     List<LogRecordOperation> operations,
                                                                     AnnotatedElementKey targetMethodKey,
                                                                     LogRecordEvaluationContext ctx) {

        if (beforeExecuteFunctions == null && beforeExecuteFunctions.isEmpty()) {
            return Collections.emptyMap();
        }

        registerSpELFunction(beforeExecuteFunctions, ctx);

        // 注册自定义函数
        parser.setLogRecordEvaluationContext(ctx);
        Map<String, String> executeTemplateAndReturnMap = new HashMap<>();

        for (LogRecordOperation op : operations) {
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
     * @param operations         目标操作集合
     * @param beforeExecuteComplete           是否成功
     * @param executeTemplateMap 提前执行的模板
     * @param targetMethodKey    目标方法标识key
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void recordExecute(SimpleLogRecordContext simpleContext,
                               List<LogRecordOperation> operations,
                               boolean beforeExecuteComplete,
                               Map<String, String> executeTemplateMap,
                               AnnotatedElementKey targetMethodKey,
                               LogRecordEvaluationContext ctx) throws NoSuchFieldException, IllegalAccessException {

        ctx.setContextVariables();
        ctx.setRetAndErrMsg(simpleContext.getRet(), simpleContext.getErrMsg());

        // 注册自定义函数
        Set<String> functionNames = filterOperationFunctionNames(false, operations);
        registerSpELFunction(functionNames, ctx);
        parser.setLogRecordEvaluationContext(ctx);

        // 持久化日志POJO
        Record record = new Record();
        Class<Record> clazz = Record.class;

        for (LogRecordOperation op : operations) {
            String key = op.getKey();
            String value = op.getValue();
            String oldTemplate = executeTemplateMap.get(key);
            if (op.isTemplate() && functionNames.containsAll(op.getFunctionNames())) {
                String template = oldTemplate != null ? oldTemplate : value;
                value = parser.parseExpression(template, targetMethodKey);
            }
            final Field declaredField = clazz.getDeclaredField(key);
            declaredField.setAccessible(true);
            declaredField.set(record, value);
        }

        // 操作用户
        String operator = record.getOperator();
        if (operator == null || operator.isEmpty()) {
            Operator user = operatorGetService.getUser();
            if (user != null) {
                record.setOperator(user.getUsername());
            }
        }

        record.setComplete(beforeExecuteComplete);
        record.setTimestamp(System.currentTimeMillis());

        // 发送消息
        logRecordPublisher.publishEvent(new LogRecordEvent(record));
    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        log.error("切面处理过程中出错, 错误原因: {}", e.getMessage());
    }

    private Set<String> filterOperationFunctionNames(boolean isBeforeExecute, List<LogRecordOperation> operations) {
        Set<String> filterFuncNames = new HashSet<>(operations.size() * 2);
        if (isBeforeExecute) {
            for (LogRecordOperation op : operations) {
                if (op.isTemplate() && op.isBeforeExecute()){
                    filterFuncNames.addAll(op.getFunctionNames());
                }
            }
        } else {
            for (LogRecordOperation op : operations) {
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
