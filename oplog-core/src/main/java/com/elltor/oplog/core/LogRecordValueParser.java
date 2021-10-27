package com.elltor.oplog.core;

import org.springframework.context.expression.AnnotatedElementKey;

/**
 * SpEL 模板解析器
 */

public class LogRecordValueParser {

    private LogRecordEvaluationContext logRecordEvaluationContext;

    private LogRecordExpressionEvaluator expressionParser;

    public LogRecordValueParser(LogRecordEvaluationContext logRecordEvaluationContext,
                                LogRecordExpressionEvaluator expressionParser) {

        this.logRecordEvaluationContext = logRecordEvaluationContext;
        this.expressionParser = expressionParser;
    }

    public String parseExpression(String expression, AnnotatedElementKey methodKey) {
        return expressionParser.parseExpression(expression, methodKey, logRecordEvaluationContext);
    }

    public void setLogRecordEvaluationContext(LogRecordEvaluationContext logRecordEvaluationContext) {
        this.logRecordEvaluationContext = logRecordEvaluationContext;
    }

}
