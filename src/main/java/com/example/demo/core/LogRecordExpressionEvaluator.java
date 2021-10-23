package com.example.demo.core;


import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogRecordExpressionEvaluator extends CachedExpressionEvaluator {

    private final static Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);

    private final static Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    public final static TemplateParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext("{", "}");

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser() {
        @Override
        public Expression parseExpression(String expressionString) throws ParseException {
            return parseExpression(expressionString, TEMPLATE_PARSER_CONTEXT);
        }
    };

    public LogRecordExpressionEvaluator() {
        super(EXPRESSION_PARSER);
    }

    EvaluationContext createEvaluationContext(Method method, Object[] args, Class<?> targetClass,Object ret, String errorMsg, String beanFactory){
        return new LogRecordEvaluationContext(TypedValue.NULL, method, args, new DefaultParameterNameDiscoverer(), ret, errorMsg);
    }


    public String parseExpression(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return getExpression(expressionCache, methodKey, conditionExpression).getValue(evalContext, String.class);
    }


}
