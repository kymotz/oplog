package com.example.demo.aspect;

import com.example.demo.entity.LogRecordOps;
import com.example.demo.entity.MethodExecuteResult;
import com.example.demo.parse.LogRecordContext;
import com.example.demo.parse.LogRecordOperationSource;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class LogRecordInterceptor implements MethodInterceptor {

    // LogRecord的解析类
    private LogRecordOperationSource logRecordOperationSource;

    private LogRecordPointcut pointcut = new LogRecordPointcut();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        // 记录日志
        return execute(invocation, invocation.getThis(), method, invocation.getArguments());
    }

    private Object execute(MethodInvocation invoker, Object target, Method method, Object[] args) throws Throwable {
        Class<?> targetClass = getTargetClass(target);
        System.out.println("targetClass#getName" + targetClass.getName());
        Object ret = null;
//        MethodExecuteResult methodExecuteResult = new MethodExecuteResult(true, null, "");
//        LogRecordContext.putEmptySpan();
//        Collection<LogRecordOps> operations = new ArrayList<>();
//        Map<String, String> functionNameAndReturnMap = new HashMap<>();
//        try {
//            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
//            List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);
//            //业务逻辑执行前的自定义函数解析
//            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(spElTemplates, targetClass, method, args);
//        } catch (Exception e) {
//            log.error("log record parse before function exception", e);
//        }
//        try {
//            ret = invoker.proceed();
//        } catch (Exception e) {
//            methodExecuteResult = new MethodExecuteResult(false, e, e.getMessage());
//        }
//        try {
//            if (!CollectionUtils.isEmpty(operations)) {
//                recordExecute(ret, method, args, operations, targetClass,
//                        methodExecuteResult.isSuccess(), methodExecuteResult.getErrorMsg(), functionNameAndReturnMap);
//            }
//        } catch (Exception t) {
//            //记录日志错误不要影响业务
//            log.error("log record parse exception", t);
//        } finally {
//            LogRecordContext.clear();
//        }
//        if (methodExecuteResult.throwable != null) {
//            throw methodExecuteResult.throwable;
//        }
        return ret;
    }

    private Class<?> getTargetClass(Object obj){
        return obj.getClass();
    }

    List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> collection){
        return null;
    }

    Map<String, String> processBeforeExecuteFunctionTemplate(List<String> spElTemplates, Class<?> targetClass, Method method, Object[] args){
        return null;
    }



    public void recordExecute(Object ret, Method method, Object[] args,Collection<LogRecordOps> operations, Class<?> targetClass, boolean isSuccess, String errMsg, Map<String, String> functionNameAndReturnMap){

    }
}
