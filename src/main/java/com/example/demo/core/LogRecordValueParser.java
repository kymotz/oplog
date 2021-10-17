package com.example.demo.core;

import lombok.Data;
import org.springframework.context.expression.AnnotatedElementKey;

@Data
public class LogRecordValueParser {
    LogRecordEvaluationContext logRecordEvaluationContext;
    LogRecordExpressionEvaluator expressionParser;

    public LogRecordValueParser(LogRecordEvaluationContext logRecordEvaluationContext, LogRecordExpressionEvaluator expressionParser) {
        this.logRecordEvaluationContext = logRecordEvaluationContext;
        this.expressionParser = expressionParser;
    }

    public String parseExpression(String expression, AnnotatedElementKey methodKey){
        System.out.println("methodKey.getClass().getSimpleName() = " + methodKey.getClass().getSimpleName());
        return expressionParser.parseExpression(expression, methodKey,logRecordEvaluationContext);
    }
}
