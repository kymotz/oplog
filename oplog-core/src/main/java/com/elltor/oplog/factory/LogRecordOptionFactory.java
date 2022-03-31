package com.elltor.oplog.factory;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.core.CustomSpelExpressionParser;
import com.elltor.oplog.entity.LogRecordOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogRecordOptionFactory {

    /**
     * 等于 @LogRecord 字段数
     */
    private final static int DEFAULT_OPS_SIZE = 10;

    /**
     * @see com.elltor.oplog.core.CustomSpelExpressionParser
     */
    private final String templatePrefix = CustomSpelExpressionParser.prefix;

    /**
     * @see com.elltor.oplog.core.CustomSpelExpressionParser
     */
    private final String templateSuffix = CustomSpelExpressionParser.suffix;

    private final char prefix = '#';

    private final char suffix = '(';

    private final String basicCharacter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY_ ";

    private static final BitSet SUPPORT_CHARS = new BitSet();

    private static final Set<String> SUPPORT_CONDITION_VALUE = new HashSet<>(4);

    private static final Set<String> TRUE_VALUE_CONDITION = new HashSet<>(2);

    private static final Set<String> FALSE_VALUE_CONDITION = new HashSet<>(2);

    private static ConcurrentHashMap<LogRecord, List<LogRecordOption>> recordOptionsCache = new ConcurrentHashMap<>();

    private Logger log = LoggerFactory.getLogger(LogRecordOptionFactory.class);

    /**
     * 解析函数工具方法
     */
    private ParseFunctionFactory parseFact;

    public LogRecordOptionFactory(ParseFunctionFactory parseFact) {
        this.parseFact = parseFact;
        StringBuilder sb = new StringBuilder();
        sb.append(basicCharacter)
                .append(prefix)
                .append(suffix)
                .append(templatePrefix)
                .append(templateSuffix);

        for (char c : sb.toString().toCharArray()) {
            SUPPORT_CHARS.set(c);
        }

        // condition 验证
        TRUE_VALUE_CONDITION.add("true");
        TRUE_VALUE_CONDITION.add("TRUE");
        FALSE_VALUE_CONDITION.add("false");
        FALSE_VALUE_CONDITION.add("FALSE");
        SUPPORT_CONDITION_VALUE.addAll(TRUE_VALUE_CONDITION);
        SUPPORT_CONDITION_VALUE.addAll(FALSE_VALUE_CONDITION);
    }

    public List<LogRecordOption> computeLogRecordOptions(LogRecord logRecord) {
        List<LogRecordOption> ops = recordOptionsCache.get(logRecord);
        if (ops != null) {
            return ops;
        }
        ops = new ArrayList<>(DEFAULT_OPS_SIZE);
        if (!logRecord.success().isEmpty()) {
            LogRecordOption op = new LogRecordOption("success", logRecord.success());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.bizNo().isEmpty()) {
            LogRecordOption op = new LogRecordOption("bizNo", logRecord.bizNo());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.operator().isEmpty()) {
            LogRecordOption op = new LogRecordOption("operator", logRecord.operator());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.category().isEmpty()) {
            LogRecordOption op = new LogRecordOption("category", logRecord.category());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.detail().isEmpty()) {
            LogRecordOption op = new LogRecordOption("detail", logRecord.detail());
            fillTemplate(op);
            ops.add(op);
        }

        // handle condition field
        processConditionField(logRecord, ops);

        if (!logRecord.fail().isEmpty()) {
            LogRecordOption op = new LogRecordOption("fail", logRecord.fail());
            fillTemplate(op);
            ops.add(op);
        }

        recordOptionsCache.put(logRecord, ops);
        return ops;
    }

    public boolean assertConditionIsTrue(String conditionValue) {
        return TRUE_VALUE_CONDITION.contains(conditionValue);
    }

    public boolean assertConditionIsFalse(String conditionValue) {
        return FALSE_VALUE_CONDITION.contains(conditionValue);
    }


    private void fillTemplate(LogRecordOption op) {
        String opValue = op.getValue();
        boolean containTemplate = opValue.contains(templatePrefix);
        op.setTemplate(containTemplate);
        if (containTemplate) {
            List<String> functionNames = parseAllTemplateMethods(opValue);
            boolean isBeforeExecute = !functionNames.isEmpty();
            for (String fn : functionNames) {
                if (!parseFact.isBeforeFunction(fn)) {
                    isBeforeExecute = false;
                    break;
                }
            }
            op.setFunctionNames(functionNames);
            op.setBeforeExecute(isBeforeExecute);
        } else {
            op.setFunctionNames(new ArrayList<>(0));
        }

        // 调用返回值的必须在方法执行完毕解析
        if (opValue.contains("#_ret")) {
            op.setBeforeExecute(false);
        }
    }

    /**
     * 解析自定义函数
     */
    private List<String> parseAllTemplateMethods(String template) {
        if (template == null || template.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> methods = new ArrayList<>();
        StringBuilder sb = new StringBuilder(128);
        int len = template.length();
        char start = template.charAt(0);
        for (int end = 0; end < len; end++) {
            char cur = template.charAt(end);
            if (cur == prefix || !SUPPORT_CHARS.get(cur)) {
                start = cur;
                sb.delete(0, sb.length());
                continue;
            }
            sb.append(cur);
            if (start == prefix && cur == suffix) {
                int sbLen = sb.length();
                if (sbLen > 1) {
                    String substring = sb.substring(0, sbLen - 1);
                    methods.add(substring.trim());
                }
                sb.delete(0, sbLen);
            }
        }
        return methods;
    }

    /**
     * 处理条件
     *
     * @param logRecord 日志注解
     * @param ops       日志注解的属性
     */
    private void processConditionField(LogRecord logRecord, List<LogRecordOption> ops) {
        LogRecordOption op;
        if (!logRecord.condition().isEmpty()) {
            op = new LogRecordOption("condition", logRecord.condition().trim());
            fillTemplate(op);
            String value = op.getValue();
            if (!SUPPORT_CONDITION_VALUE.contains(value)) {
                if (op.getFunctionNames().isEmpty()) {
                    // 条件值不为空并且解析函数为空此时条件无效
                    log.error("[2]注解的 condition 字段存在错误，不符合规范，本条件日志不记录。注解信息 : {}", logRecord);
                    fillBadCondition(op);
                }
            }
        } else {
            // 值为空字符串，默认条件无效
            op = new LogRecordOption("condition", "false");
            fillBadCondition(op);
        }
        ops.add(op);
    }

    private void fillBadCondition(LogRecordOption op) {
        op.setValue("false");
        op.setFunctionNames(Collections.emptyList());
        op.setTemplate(false);
        op.setBeforeExecute(false);
    }

// test
//    public static void main(String[] args) {
//        String str = "res : #calc(#name,'asdf'，,#user.isAdmin(),#())------++++))))((()))#(#(  #isAdmin(#currentUser())";
//        List<String> strings = parseAllTemplateMethods(str);
//        System.out.println(strings);
//    }

}
