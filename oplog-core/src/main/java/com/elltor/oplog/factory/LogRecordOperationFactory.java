package com.elltor.oplog.factory;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.entity.LogRecordOps;

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

    private String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY_";

    private static Set<Character> supportChars;

    private static ConcurrentHashMap<LogRecord, List<LogRecordOps>> recordOperatorsCache = new ConcurrentHashMap<>();

    public LogRecordOperationFactory() {
        supportChars = new HashSet<>();
        StringBuilder sb = new StringBuilder(128);
        sb.append(base)
                .append(prefix)
                .append(suffix)
                .append(templatePrefix)
                .append(templateSuffix);

        for (char c : sb.toString().toCharArray()) {
            supportChars.add(c);
        }
    }

    public List<LogRecordOps> computeLogRecordOperations(LogRecord logRecord) {
        List<LogRecordOps> ops = recordOperatorsCache.get(logRecord);
        if (ops != null) {
            return ops;
        }
        ops = new ArrayList<>(DEFAULT_OPS_SIZE);
        if (!logRecord.success().isEmpty()) {
            LogRecordOps op = new LogRecordOps("success", logRecord.success());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.bizNo().isEmpty()) {
            LogRecordOps op = new LogRecordOps("bizNo", logRecord.bizNo());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.operator().isEmpty()) {
            LogRecordOps op = new LogRecordOps("operator", logRecord.operator());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.category().isEmpty()) {
            LogRecordOps op = new LogRecordOps("category", logRecord.category());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.detail().isEmpty()) {
            LogRecordOps op = new LogRecordOps("detail", logRecord.detail());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.condition().isEmpty()) {
            LogRecordOps op = new LogRecordOps("condition", logRecord.condition());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.fail().isEmpty()) {
            LogRecordOps op = new LogRecordOps("fail", logRecord.fail());
            fillTemplate(op);
            ops.add(op);
        }
        recordOperatorsCache.put(logRecord, ops);
        return ops;
    }

    private void fillTemplate(LogRecordOps op) {
        String opValue = op.getValue();
        boolean containTemplate = opValue.contains(templatePrefix);
        op.setTemplate(containTemplate);
        if (containTemplate) {
            List<String> functionNames = parseAllTemplateMethods(opValue);
            op.setFunctionNames(functionNames);
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
            if (cur == prefix || !supportChars.contains(cur)) {
                start = cur;
                sb.delete(0, sb.length());
                continue;
            }
            sb.append(cur);
            if (start == prefix && cur == suffix) {
                int sbLen = sb.length();
                if (sbLen > 1) {
                    String substring = sb.substring(0, sbLen - 1);
                    methods.add(substring);
                }
                sb.delete(0, sbLen);
            }
        }
        return methods;
    }

// test code
//    public static void main(String[] args) {
//        String str = "res : #calc(#name,'asdf'，,#user.isAdmin(),#())------++++))))((()))#(#(  #isAdmin(#currentUser())";
//        List<String> strings = parseAllTemplateMethods(str);
//        System.out.println(strings);
//    }

}
