package com.elltor.oplog.core;

import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;


public class CustomSpelExpressionParser extends SpelExpressionParser {

    private String prefix = "{";

    private String suffix = "}";

    private TemplateParserContext templateParserContext;

    public CustomSpelExpressionParser() {
        templateParserContext = new TemplateParserContext(prefix, suffix);
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        return parseExpression(expressionString, templateParserContext);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
