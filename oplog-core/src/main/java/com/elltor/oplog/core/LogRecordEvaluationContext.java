package com.elltor.oplog.core;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Map;

public class LogRecordEvaluationContext extends MethodBasedEvaluationContext {

    public LogRecordEvaluationContext(Object rootObject, Method method,
                                      Object[] arguments, ParameterNameDiscoverer parameterNameDiscoverer,
                                      Object ret, String errMsg) {

        //把方法的参数都放到 SpEL 解析的 RootObject 中
        super(rootObject, method, arguments, parameterNameDiscoverer);
        //把 LogRecordContext 中的变量都放到 RootObject 中
        Map<String, Object> variables = LogRecordContext.getVariables();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariable(entry.getKey(), entry.getValue());
        }
        //把方法的返回值和 ErrorMsg 都放到 RootObject 中
        setVariable("_ret", ret);
        setVariable("_errMsg", errMsg);
    }

}