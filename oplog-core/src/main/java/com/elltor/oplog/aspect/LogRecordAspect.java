package com.elltor.oplog.aspect;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.core.LogRecordContext;
import com.elltor.oplog.core.LogRecordEvaluationContext;
import com.elltor.oplog.core.LogRecordValueParser;
import com.elltor.oplog.entity.*;
import com.elltor.oplog.factory.LogRecordOptionFactory;
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

    private Logger log = LoggerFactory.getLogger(LogRecordAspect.class);

    /**
     * 日志处理工程
     */
    @Resource
    private LogRecordOptionFactory optionFactory;

    /**
     * 模板解析器
     */
    @Resource
    private LogRecordValueParser parser;

    /**
     * 解析函数工具方法
     */
    @Resource
    private ParseFunctionFactory parseFunctionFactory;

    /**
     * 扩展点, 获取当前操作
     */
    @Resource
    private IOperatorGetService operatorGetService;

    /**
     * 记录日志事件发布器
     */
    @Resource
    private LogRecordPublisher logRecordPublisher;

    /**
     * 缓存日志注解, 避免重复解析 SpEL 模板
     */
    private final ConcurrentHashMap<AnnotatedElementKey, LogRecord>
            targetAnnotationCache = new ConcurrentHashMap<>();

    /**
     * 注解切入点, 增加 @LogRecord 的方法
     */
    @Pointcut("@annotation(com.elltor.oplog.annotation.LogRecord)")
    public void pointCut() { /*仅作为切面调用标记*/}

    /**
     * 增强方法
     *
     * @param point 切入点
     * @return Object
     */
    @Around(value = "pointCut()")
    public Object logRecordAround(ProceedingJoinPoint point) throws Throwable {
        return processRecordLog(point);
    }

    /**
     * 处理切面日志核心方法
     */
    private Object processRecordLog(ProceedingJoinPoint point) throws Throwable {
        // 方法、入参、注解等数据
        Method targetMethod = ((MethodSignature) point.getSignature()).getMethod();
        Class<?> targetClass = point.getTarget().getClass();
        AnnotatedElementKey targetMethodKey = new AnnotatedElementKey(targetMethod, targetClass);
        LogRecord logRecord = getTargetAnnotationOrUpdateCache(targetMethodKey, point);

        // 判断是否记录日志，condition = "false"结束执行
        String conditionValue = logRecord.condition().trim();
        if (optionFactory.assertConditionIsFalse(conditionValue) || conditionValue.isEmpty()
                || conditionValue.isBlank()) {

            return point.proceed();
        }

        // 日志上下文BO
        SimpleLogRecordContext simpleContext = new SimpleLogRecordContext();
        simpleContext.setArgs(point.getArgs());
        simpleContext.setTargetMethod(targetMethod);
        simpleContext.setMethodKey(targetMethodKey);
        simpleContext.setLogRecord(logRecord);

        // 返回值
        Object ret = null;
        // 处理结果BO
        ProceedResult proceedResult = new ProceedResult(true, null, "");
        // SpEL解析上下文
        LogRecordEvaluationContext ctx = new LogRecordEvaluationContext(simpleContext);
        // 解析的LogRecord的属性
        List<LogRecordOption> optionList;
        Map<String, String> functionNameAndReturnMap = new HashMap<>();
        optionList = optionFactory.computeLogRecordOptions(logRecord);
        // 过滤提前执行的函数
        Set<String> beforeExecuteFunctionNames = filterOptionOfFunctionNames(true, optionList);

        // 业务逻辑执行前的自定义函数 解析
        try {
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(beforeExecuteFunctionNames,
                    optionList, targetMethodKey, ctx);

        } catch (Exception e) {
            proceedResult = new ProceedResult(false, e, "前置函数执行出错");
        }

        // 执行方法前入栈
        LogRecordContext.putSpan();
        try {
            ret = point.proceed();
        } catch (Throwable e) {
            proceedResult = new ProceedResult(false, e, "注解标注的方法执行出错");
        }
        try {
            simpleContext.setRet(ret);
            simpleContext.setErrMsg(proceedResult.getErrMsg());

            // 持久化记录日志
            recordExecute(simpleContext, optionList, proceedResult.isSuccess(),
                    functionNameAndReturnMap, targetMethodKey, ctx);

        } catch (Exception t) {
            proceedResult = new ProceedResult(false, t, "持久化存储日志方法执行出错");
        } finally {
            // 出栈
            LogRecordContext.clear();
        }
        // 处理错误
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
     * @param optionList             模板操作集合
     * @param targetMethodKey        目标方法标识key
     * @return 处理的模板操作的key与处理后模板的映射map
     */
    private Map<String, String> processBeforeExecuteFunctionTemplate(Set<String> beforeExecuteFunctions,
                                                                     List<LogRecordOption> optionList,
                                                                     AnnotatedElementKey targetMethodKey,
                                                                     LogRecordEvaluationContext ctx) {

        if (beforeExecuteFunctions.isEmpty()) {
            return Collections.emptyMap();
        }

        registerSpelFunction(beforeExecuteFunctions, ctx);
        // 注册自定义函数
        parser.setLogRecordEvaluationContext(ctx);
        Map<String, String> executeTemplateAndReturnMap = new HashMap<>();
        for (LogRecordOption op : optionList) {
            if (!op.isTemplate()) {
                continue;
            }
            List<String> functionNames = op.getFunctionNames();
            if ((!functionNames.isEmpty()) && beforeExecuteFunctions.containsAll(functionNames)) {
                String newTemplate = parser.parseExpression(op.getValue(), targetMethodKey);
                executeTemplateAndReturnMap.put(op.getKey(), newTemplate);
            }
        }
        return executeTemplateAndReturnMap;
    }

    /**
     * 持久化存储操作日志
     *
     * @param optionList            目标操作集合
     * @param beforeExecuteComplete 是否成功
     * @param executeTemplateMap    提前执行的模板
     * @param targetMethodKey       目标方法标识key
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void recordExecute(SimpleLogRecordContext simpleContext, List<LogRecordOption> optionList, boolean beforeExecuteComplete, Map<String, String> executeTemplateMap, AnnotatedElementKey targetMethodKey, LogRecordEvaluationContext ctx) throws NoSuchFieldException, IllegalAccessException {

        ctx.setContextVariables();
        ctx.setRetAndErrMsg(simpleContext.getRet(), simpleContext.getErrMsg());

        // 注册自定义函数
        Set<String> functionNames = filterOptionOfFunctionNames(false, optionList);
        registerSpelFunction(functionNames, ctx);
        parser.setLogRecordEvaluationContext(ctx);

        // 持久化日志POJO
        Record record = new Record();
        Class<Record> clazz = Record.class;

        for (LogRecordOption op : optionList) {
            String key = op.getKey();
            String value = op.getValue();
            String oldTemplate = executeTemplateMap.get(key);
            // TODO  isTemplate 可以优化掉
            if (op.isTemplate() && functionNames.containsAll(op.getFunctionNames())) {
                String template = oldTemplate != null ? oldTemplate : value;
                value = parser.parseExpression(template, targetMethodKey);
            }
            Field declaredField = clazz.getDeclaredField(key);
            declaredField.setAccessible(true);
            declaredField.set(record, value);
        }

        // 根据解析条件是否发送日志消息
        if (!optionFactory.assertConditionIsTrue(record.getCondition())) {
            return;
        }

        // 操作用户
        String operator = record.getOperator();
        if (operator == null || operator.isEmpty()) {
            Operator user = operatorGetService.getUser();
            if (user != null) {
                record.setOperator(user.getUsername());
            }
        }

        String successMsg = record.getSuccess();
        record.setComplete(beforeExecuteComplete && !successMsg.isEmpty());
        record.setTimestamp(System.currentTimeMillis());

        // 发送记录日志的消息
        logRecordPublisher.publishEvent(new LogRecordEvent(record));
    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        log.error("切面处理过程中出错, 错误原因: {}", e.getMessage());
    }

    /**
     * 过滤执行函数名
     *
     * @param isBeforeExecute   是否在方法执行前执行
     * @param optionList        注解的选项
     */
    private Set<String> filterOptionOfFunctionNames(boolean isBeforeExecute, List<LogRecordOption> optionList) {
        Set<String> filterFuncNames = new HashSet<>();
        if (isBeforeExecute) {
            for (LogRecordOption op : optionList) {
                if (op.isTemplate() && op.isBeforeExecute()) {
                    filterFuncNames.addAll(op.getFunctionNames());
                }
            }
        } else {
            for (LogRecordOption op : optionList) {
                filterFuncNames.addAll(op.getFunctionNames());
            }
        }
        return filterFuncNames;
    }

    /**
     *  在 SpEL 解析的上下文注册自定义函数
     *
     * @param registerFunctions 要注册的函数名集合
     * @param ctx               SpEL解析上下文
     */
    private void registerSpelFunction(Set<String> registerFunctions, LogRecordEvaluationContext ctx) {
        Iterator<String> it = registerFunctions.iterator();
        while (it.hasNext()) {
            String funcName = it.next();
            IParseFunction function = parseFunctionFactory.getFunction(funcName);
            if (function == null) {
                it.remove();
                continue;
            }
            ctx.registerFunction(funcName, function.functionMethod());
        }
    }

    /**
     * 获取或更新缓存的注解
     *
     * @param cacheKey  key
     * @param point     切入点
     * @return  @LogRecord
     */
    private LogRecord getTargetAnnotationOrUpdateCache(AnnotatedElementKey cacheKey, ProceedingJoinPoint point) {

        LogRecord targetAnnotation = targetAnnotationCache.get(cacheKey);
        if (targetAnnotation != null) {
            return targetAnnotation;
        }

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method targetMethod = signature.getMethod();
        LogRecord logRecord = targetMethod.getAnnotation(LogRecord.class);
        targetAnnotationCache.put(cacheKey, logRecord);
        return logRecord;
    }

}
