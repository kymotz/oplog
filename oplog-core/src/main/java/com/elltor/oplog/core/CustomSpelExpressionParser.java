package com.elltor.oplog.core;

import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 自定义 SpEL 解析器
 */
public class CustomSpelExpressionParser extends SpelExpressionParser {

    /**
     * EL前缀
     */
    public static final String prefix = "{";

    /**
     * EL后缀
     */
    public static final String suffix = "}";

    private TemplateParserContext templateParserContext;

    public CustomSpelExpressionParser() {
        templateParserContext = new TemplateParserContext(prefix, suffix);
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        return parseExpression(expressionString, templateParserContext);
    }

}
