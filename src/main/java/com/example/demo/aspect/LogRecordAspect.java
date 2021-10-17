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

    IFunctionService functionService = new DefaultFunctionServiceImpl(new ParseFunctionFactory(Arrays.asList(new IParseFunction() {
        //LogRecordValueParser logRecordValueParser = new LogRecordValueParser()
        @Override
        public String functionName() {
            return "calc";
        }
        @Override
        public String apply(String value) {

            return "500";
        }
    })));


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

            // 生成teamplate，
            List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);

            // 处理spel模版，把非模版的也解析出来
            // 业务逻辑执行前的自定义函数 解析
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(operations, spElTemplates, targetClass, method, args);
            for (Map.Entry<String, String> entry : functionNameAndReturnMap.entrySet()) {
                System.out.println(entry.getKey()+"  "+entry.getValue());
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

    private List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> operations) {
        List<String> templates = new ArrayList<>();
        for (LogRecordOps o : operations) {
            templates.add(o.getValue());
        }
        return templates;
    }

    private Map<String, String> processBeforeExecuteFunctionTemplate(List<LogRecordOps> ops, List<String> spElTemplates,
                                                                     Class<?> targetClass, Method method, Object[] args) {

        final LogRecordEvaluationContext logRecordEvaluationContext = new LogRecordEvaluationContext(TypedValue.NULL,
                method, args, new DefaultParameterNameDiscoverer(), null, null);



        LogRecordValueParser logRecordParser = new LogRecordValueParser(logRecordEvaluationContext, logRecordExpressionEvaluator);
        Map<String, String> functionNameAndReturnMap = new HashMap<>();

        for (String template : spElTemplates) {
            AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
            // process
//            final String name = functionService.apply("getUserName", op.getValue());
//            String parseStr = logRecordParser.parseExpression(op.getValue(), methodKey);
//            functionNameAndReturnMap.put(op.getKey(), parseStr);
        }
        return functionNameAndReturnMap;
    }

    private void recordExecute(Object ret, Method method, Object[] args, Collection<LogRecordOps> operations, Class<?> targetClass, boolean success, String errMsg, Map<String, String> functionNameAndReturnMap) {
        final LogRecordEvaluationContext ctx = new LogRecordEvaluationContext(TypedValue.NULL, method, args, new DefaultParameterNameDiscoverer(), ret, errMsg);
        //


        for(LogRecordOps op : operations){

        }
        logRecordService.record(new Record());
    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        e.printStackTrace();
        System.out.println("报错了; e=" + e.getMessage());
    }
}
