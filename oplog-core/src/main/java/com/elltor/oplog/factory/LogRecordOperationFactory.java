package com.elltor.oplog.factory;

import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.entity.LogRecordOperation;

import javax.annotation.Resource;
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

    private static ConcurrentHashMap<LogRecord, List<LogRecordOperation>> recordOperatorsCache = new ConcurrentHashMap<>();

    /**
     * 解析函数工具方法
     */
    private ParseFunctionFactory parseFact;

    public LogRecordOperationFactory(ParseFunctionFactory parseFact) {
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
        this.parseFact = parseFact;
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
        if (!logRecord.condition().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("condition", logRecord.condition());
            fillTemplate(op);
            ops.add(op);
        }
        if (!logRecord.fail().isEmpty()) {
            LogRecordOperation op = new LogRecordOperation("fail", logRecord.fail());
            fillTemplate(op);
            ops.add(op);
        }
        recordOperatorsCache.put(logRecord, ops);
        return ops;
    }

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
