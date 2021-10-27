package com.elltor.oplog.factory;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.entity.LogRecordOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogRecordOperationFactory {

    private final static int DEFAULT_OPS_SIZE = 7;

    /**
     * @see com.elltor.oplog.core.CustomSpelExpressionParser
     */
    private final String templatePrefix = "{";

    /**
     * @see com.elltor.oplog.core.CustomSpelExpressionParser
     */
    private final String templateSuffix = "}";

    private char prefix = '#';

    private char suffix = '(';

    private String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY_ ";

    private static final BitSet SUPPORT_CHARS = new BitSet();

    private static final Set<String> SUPPORT_CONDITION = new HashSet<>(4);

    private static final Set<String> TRUE_CONDITION = new HashSet<>(2);

    private static final Set<String> FALSE_CONDITION = new HashSet<>(2);

    private static ConcurrentHashMap<LogRecord, List<LogRecordOperation>> recordOperatorsCache = new ConcurrentHashMap<>();

    private Logger log = LoggerFactory.getLogger(LogRecordOperationFactory.class);

    /**
     * 解析函数工具方法
     */
    private ParseFunctionFactory parseFact;

    public LogRecordOperationFactory(ParseFunctionFactory parseFact) {
        this.parseFact = parseFact;
        StringBuilder sb = new StringBuilder(128);
        sb.append(base)
                .append(prefix)
                .append(suffix)
                .append(templatePrefix)
                .append(templateSuffix);

        for (char c : sb.toString().toCharArray()) {
            SUPPORT_CHARS.set(c);
        }

        // condition
        TRUE_CONDITION.add("true");
        TRUE_CONDITION.add("TRUE");
        FALSE_CONDITION.add("false");
        FALSE_CONDITION.add("FALSE");
        SUPPORT_CONDITION.addAll(TRUE_CONDITION);
        SUPPORT_CONDITION.addAll(FALSE_CONDITION);

    }

    public List<LogRecordOperation> computeLogRecordOperations(LogRecord logRecord) {
        List<LogRecordOperation> ops = recordOperatorsCache.get(logRecord);
        if (ops != null) {
            return ops;
        }
        ops = new ArrayList<>(DEFAULT_OPS_SIZE);
        if (!logRecord.success().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("success", logRecord.success());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.bizNo().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("bizNo", logRecord.bizNo());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.operator().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("operator", logRecord.operator());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.category().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("category", logRecord.category());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.detail().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("detail", logRecord.detail());
            fillTemplate(op);
            ops.add(op);
        }

        // handle condition field
        processConditionField(logRecord, ops);

        if (!logRecord.fail().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("fail", logRecord.fail());
            fillTemplate(op);
            ops.add(op);
        }

        recordOperatorsCache.put(logRecord, ops);
        return ops;
    }

    public boolean assertConditionIsTrue(String conditionValue) {
        return TRUE_CONDITION.contains(conditionValue);
    }

    public boolean assertConditionIsFalse(String conditionValue) {
        return FALSE_CONDITION.contains(conditionValue);
    }

    // private methods separator

    private void fillTemplate(LogRecordOperation op) {
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

    private void processConditionField(LogRecord logRecord, List<LogRecordOperation> ops) {
        LogRecordOperation op;
        if (!logRecord.condition().isEmpty()) {
            op = new LogRecordOperation("condition", logRecord.condition().trim());
            fillTemplate(op);
            String value = op.getValue();
            if (!SUPPORT_CONDITION.contains(value)) {
                if (op.getFunctionNames().isEmpty()) {
                    // 条件值不为空并且解析函数为空此时条件无效
                    log.error("[2]注解的 condition 字段存在错误，不符合规范，本条件日志不记录。注解信息 : {}", logRecord);
                    fillBadConditionOperation(op);
                }
            }
        } else {
            // 值为空字符串，默认条件无效
            op = new LogRecordOperation("condition", "false");
            fillBadConditionOperation(op);
        }
        ops.add(op);
    }

    private void fillBadConditionOperation(LogRecordOperation op) {
        op.setValue("false");
        op.setFunctionNames(Collections.emptyList());
        op.setTemplate(false);
        op.setBeforeExecute(false);
    }

// test code
//    public static void main(String[] args) {
//        String str = "res : #calc(#name,'asdf'，,#user.isAdmin(),#())------++++))))((()))#(#(  #isAdmin(#currentUser())";
//        List<String> strings = parseAllTemplateMethods(str);
//        System.out.println(strings);
//    }

}
