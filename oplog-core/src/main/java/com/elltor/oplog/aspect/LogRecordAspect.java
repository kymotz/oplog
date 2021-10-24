package com.elltor.oplog.aspect;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.core.*;
import com.elltor.oplog.service.IParseFunction;
import com.elltor.oplog.entity.LogRecordOps;
import com.elltor.oplog.entity.MethodProceedResult;
import com.elltor.oplog.entity.Record;
import com.elltor.oplog.factory.ParseFunctionFactory;
import com.elltor.oplog.service.ILogRecordService;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
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

@Component
@Aspect
@Slf4j
public class LogRecordAspect {

    @Resource
    private LogRecordOperationSource logRecordOperationSource;

    // 持久化日志日志
    @Resource
    private ILogRecordService logRecordService;

    @Resource
    private LogRecordExpressionEvaluator logRecordExpressionEvaluator;

    @Resource
    private LogRecordValueParser parser;

    @Resource
    private ParseFunctionFactory fact;

    private ConcurrentHashMap<AnnotatedElementKey, LogRecord> targetAnnotationCache = new ConcurrentHashMap<>();

    private ParameterNameDiscoverer parameterNameDiscover = new DefaultParameterNameDiscoverer();

    // 注解切入
    @Pointcut("@annotation(com.elltor.oplog.annotation.LogRecord)")
    public void pointCut() {
    }

    @Around(value = "pointCut()")
    public Object logRecordAround(ProceedingJoinPoint point) throws Throwable {
        System.out.println("point.getArgs() = " + Arrays.toString(point.getArgs()));
        return recordLog(point);
    }

    private Object recordLog(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        Method targetMethod = ((MethodSignature) point.getSignature()).getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        AnnotatedElementKey targetMethodKey = new AnnotatedElementKey(targetMethod, targetClass);

        LogRecord logRecord = getTargetAnnotationOrUpdateCache(targetMethodKey, point);

        Object ret = null;

        if (logRecord == null) {
            return null;
        }

        MethodProceedResult methodProceedResult = new MethodProceedResult(true, null, "");

        // 执行方法前入栈空map
        LogRecordContext.putEmptySpan();

        // 解析的LogRecord的属性
        List<LogRecordOps> operators = new ArrayList<>();

        Map<String, String> functionNameAndReturnMap = new HashMap<>();

        try {
            operators = logRecordOperationSource.computeLogRecordOperations(logRecord);
            for (LogRecordOps op : operators) {
                System.out.println(op);
            }
            System.out.println("----");

            List<String> beforeExecuteFunctionNames = filterOperateFunctionNames(true, operators);

            // 处理spel模版，把非模版的也解析出来
            // 业务逻辑执行前的自定义函数 解析
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(beforeExecuteFunctionNames, operators,
                    targetMethod, args, targetMethodKey);

            for (Map.Entry<String, String> entry : functionNameAndReturnMap.entrySet()) {
                System.out.println(entry.getKey() + "  " + entry.getValue());
            }
        } catch (Exception e) {
            log.error("log record parse before function exception", e);
        }
        try {
            ret = point.proceed();
        } catch (Throwable e) {
            methodProceedResult = new MethodProceedResult(false, e, e.getMessage());
        }
        try {
            // 持久化记录日志
            if (!CollectionUtils.isEmpty(operators)) {
                recordExecute(ret, targetMethod, args, operators,
                        methodProceedResult.isSuccess(), methodProceedResult.getErrMsg(),
                        functionNameAndReturnMap, targetMethodKey);
            }
        } catch (Exception t) {
            //记录日志错误不要影响业务
            log.error("log record parse exception", t);
        } finally {
            LogRecordContext.clear();
        }
        if (methodProceedResult.getThrowable() != null) {
            throw methodProceedResult.getThrowable();
        }
        return ret;
    }

    private Map<String, String> processBeforeExecuteFunctionTemplate(List<String> beforeExecuteFunctions,
                                                                     List<LogRecordOps> operators,
                                                                     Method method,
                                                                     Object[] args,
                                                                     AnnotatedElementKey targetMethodKey) {

        if (beforeExecuteFunctions == null && beforeExecuteFunctions.isEmpty()) {
            return new HashMap<>(0);
        }

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                method, args, parameterNameDiscover, null, null);

        registerSpELFunction(beforeExecuteFunctions, logRecordEvaluationContext);

        // 注册自定义函数
        parser.setLogRecordEvaluationContext(logRecordEvaluationContext);
        Map<String, String> executeTemplateAndReturnMap = new HashMap<>();

        for (LogRecordOps op : operators) {
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

    private void recordExecute(Object ret, Method method,
                               Object[] args, List<LogRecordOps> operators,
                               boolean success,
                               String errMsg, Map<String, String> executeTemplateMap,
                               AnnotatedElementKey targetMethodKey) throws NoSuchFieldException, IllegalAccessException {

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                method, args, parameterNameDiscover, ret, errMsg);

        List<String> functionNames = filterOperateFunctionNames(false, operators);

        registerSpELFunction(functionNames, logRecordEvaluationContext);

        parser.setLogRecordEvaluationContext(logRecordEvaluationContext);

        Record record = new Record();
        Class<Record> clazz = Record.class;

        for (LogRecordOps op : operators) {
            String key = op.getKey();
            String value;
            String oldTemplate = executeTemplateMap.get(key);
            if (op.isTemplate()) {
                if (oldTemplate != null) {
                    value = parser.parseExpression(oldTemplate, targetMethodKey);
                } else {
                    value = parser.parseExpression(op.getValue(), targetMethodKey);
                }
            } else {
                value = op.getValue();
            }
            final Field declaredField = clazz.getDeclaredField(key);
            declaredField.setAccessible(true);
            declaredField.set(record, value);
        }

        logRecordService.record(record);
    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        e.printStackTrace();
        System.out.println("报错了; e=" + e.getMessage());
    }

    private List<String> filterOperateFunctionNames(boolean isBeforeExecute,
                                                    List<LogRecordOps> operators) {

        List<String> filterFuncNames = new ArrayList<>(operators.size() * 2);
        if (isBeforeExecute) {
            for (LogRecordOps op : operators) {
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
            for (LogRecordOps op : operators) {
                filterFuncNames.addAll(op.getFunctionNames());
            }
        }
        return filterFuncNames;
    }

    private void registerSpELFunction(List<String> registerFunctions,
                                      LogRecordEvaluationContext ctx) {

        Iterator<String> it = registerFunctions.iterator();
        while (it.hasNext()) {
            String fn = it.next();
            IParseFunction function = fact.getFunction(fn);
            if (function == null) {
                it.remove();
                log.error("bad 非法 函数！！！！");
                continue;
            }
            System.out.println("函数名 " + fn);
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
