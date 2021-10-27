package com.elltor.oplog.core;

import com.elltor.oplog.entity.SimpleLogRecordContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.util.Map;

/**
 * SpEL上下文解析类
 */

public class LogRecordEvaluationContext extends MethodBasedEvaluationContext {

    private static final ParameterNameDiscoverer PARAM_DISCOVER = new DefaultParameterNameDiscoverer();

    public LogRecordEvaluationContext(SimpleLogRecordContext ctx) {
        //把方法的参数都放到 SpEL 解析的 RootObject 中
        super(ctx.getRootObject(), ctx.getTargetMethod(), ctx.getArgs(), PARAM_DISCOVER);
    }

    /**
     * 把 LogRecordContext 中的变量都放到 RootObject 中 <p>
     * LogRecord标注的方法执行后，上下文中才可能存在值
     */
    public void setContextVariables(){
        Map<String, Object> variables = LogRecordContext.getVariables();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariable(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 把方法的返回值和 errMsg 都放到 RootObject 中
     * LogRecord标注的方法执行后，上下文中才可能存在返回值和错误信息
     *
     * @param ret       返回值
     * @param errMsg    错误信息
     */
    public void setRetAndErrMsg(Object ret, String errMsg) {
        if(ret != null){
            setVariable("_ret", ret);
        }
        if(errMsg != null){
            setVariable("_errMsg", errMsg);
        }
    }

}