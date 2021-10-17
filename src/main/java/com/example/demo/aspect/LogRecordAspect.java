package com.example.demo.aspect;

import com.example.demo.annotation.LogRecord;
import com.example.demo.core.*;
import com.example.demo.entity.LogRecordOps;
import com.example.demo.entity.MethodProceedResult;
import com.example.demo.entity.Record;
import com.example.demo.factory.ParseFunctionFactory;
import com.example.demo.service.IFunctionService;
import com.example.demo.service.ILogRecordService;
import com.example.demo.service.IParseFunction;
import com.example.demo.service.impl.DefaultFunctionServiceImpl;
import com.example.demo.service.impl.DefaultLogRecordServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.TypedValue;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Component
@Aspect
@Slf4j
public class LogRecordAspect {

    LogRecordOperationSource logRecordOperationSource = new LogRecordOperationSource();

    // 持久化日志日志
    ILogRecordService logRecordService = new DefaultLogRecordServiceImpl();

    LogRecordExpressionEvaluator logRecordExpressionEvaluator = new LogRecordExpressionEvaluator();

    LogRecordValueParser parser = new LogRecordValueParser(null, logRecordExpressionEvaluator);


    ParseFunctionFactory fact = new ParseFunctionFactory(Arrays.asList(new IParseFunction() {
        //LogRecordValueParser logRecordValueParser = new LogRecordValueParser()
        @Override
        public String functionName() {
            return "calc";
        }

        @Override
        public String apply(String value) {

            return "500";
        }
    }));

    IFunctionService functionService = new DefaultFunctionServiceImpl(fact);

    // 注解切入
    @Pointcut("@annotation(com.example.demo.annotation.LogRecord)")
    public void pointCut() {
    }

    @Around(value = "pointCut()")
    public Object logRecordAround(ProceedingJoinPoint point) throws Throwable {
        System.out.println("point.getArgs() = " + Arrays.toString(point.getArgs()));
        return recordLog(point);
    }

    private Object recordLog(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 标记@LogRecord的方法
        Method method = signature.getMethod();
        System.out.println("method.getName() = " + method.getName());
        // 标记@LogRecord的方法的class，即OrderServiceImpl
        Class<?> targetClass = point.getTarget().getClass();
//        System.out.println("targetClass.getSimpleName() = " + targetClass.getSimpleName());
        System.out.println("----");
        // 标记@LogRecord的方法的参数
        Object[] args = point.getArgs();
        Object ret = null;
        LogRecord logRecord = method.getAnnotation(LogRecord.class);
        if (logRecord == null) {
            return null;
        }

        MethodProceedResult methodProceedResult = new MethodProceedResult(true, null, "");

        // 执行方法前入栈空map
        LogRecordContext.putEmptySpan();

        // 解析的LogRecord的属性
        List<LogRecordOps> operations = new ArrayList<>();

        Map<String, String> functionNameAndReturnMap = new HashMap<>();

        try {
            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
            System.out.println(Arrays.toString(operations.toArray()));

            // 生成提前运行的功能名字
            List<String> functionNames = getBeforeExecuteFunctionName(operations);

            // 处理spel模版，把非模版的也解析出来
            // 业务逻辑执行前的自定义函数 解析
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(operations, functionNames, targetClass, method, args);
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
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(ret, method, args, operations, targetClass,
                        methodProceedResult.isSuccess(), methodProceedResult.getErrMsg(), functionNameAndReturnMap);
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

    private List<String> getBeforeExecuteFunctionName(List<LogRecordOps> operations) {
        List<String> functionNames = new ArrayList<>();
        for (LogRecordOps o : operations) {
            if (o.isTemplate() && o.isBeforeExecute()) {
                for (String method : o.getFunctionNames()) {
                    if (functionService.beforeFunction(method)) {
                        functionNames.add(method);
                    }
                }
            }
        }
        return functionNames;
    }

    private Map<String, String> processBeforeExecuteFunctionTemplate(List<LogRecordOps> ops, List<String> functionNames,
                                                                     Class<?> targetClass, Method method, Object[] args) {

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                method, args, new DefaultParameterNameDiscoverer(), null, null);


        try {
            for (String func : functionNames) {
                IParseFunction function = fact.getFunction(func);
                if (function != null) {
                    logRecordEvaluationContext.registerFunction(func, function.getClass().getMethod("apply", String.class));
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 注册自定义函数
        parser.setLogRecordEvaluationContext(logRecordEvaluationContext);
        Map<String, String> executeTemplateAndReturnMap = new HashMap<>();

        // target method class
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        for (LogRecordOps op : ops) {
            if (op.isTemplate() && op.isBeforeExecute()) {
                String newTemplate = parser.parseExpression(op.getValue(), methodKey);
                executeTemplateAndReturnMap.put(op.getKey(), newTemplate);
            }
        }
        return executeTemplateAndReturnMap;
    }

    private void recordExecute(Object ret, Method method,
                               Object[] args, Collection<LogRecordOps> operations,
                               Class<?> targetClass, boolean success,
                               String errMsg, Map<String, String> executeTemplateMap) throws NoSuchFieldException, IllegalAccessException {

        final LogRecordEvaluationContext ctx = new LogRecordEvaluationContext(TypedValue.NULL, method,
                args, new DefaultParameterNameDiscoverer(), ret, errMsg);

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                method, args, new DefaultParameterNameDiscoverer(), null, null);

        for (LogRecordOps op : operations) {
            if (op.isTemplate()) {
                try {
                    for (String fn : op.getFunctionNames()) {
                        IParseFunction func = fact.getFunction(fn);

                        logRecordEvaluationContext.registerFunction(fn,
                                func.getClass().getMethod("apply", String.class));
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        parser.setLogRecordEvaluationContext(ctx);

        // target method class
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);

        Record record = new Record();
        Class<Record> clazz = Record.class;

        for (LogRecordOps op : operations) {
            String newTemplate = executeTemplateMap.get(op.getKey());
            String str;
            if(newTemplate != null){
                str = parser.parseExpression(newTemplate, methodKey);
            }else{
                str = parser.parseExpression(op.getValue(), methodKey);
            }
            Field declaredField = clazz.getDeclaredField(op.getKey());
            declaredField.setAccessible(true);
            declaredField.set(record, str);
        }

        logRecordService.record(record);
    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        e.printStackTrace();
        System.out.println("报错了; e=" + e.getMessage());
    }
}
