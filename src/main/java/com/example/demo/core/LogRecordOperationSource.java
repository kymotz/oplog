package com.example.demo.core;

import com.example.demo.annotation.LogRecord;
import com.example.demo.entity.LogRecordOps;

import java.lang.reflect.Method;
import java.util.*;

public class LogRecordOperationSource {

    private final static int DEFAULT_OPS_SIZE = 7;

    private final String templatePrefix = LogRecordExpressionEvaluator.TEMPLATE_PARSER_CONTEXT.getExpressionPrefix();
    private final String templateSuffix = LogRecordExpressionEvaluator.TEMPLATE_PARSER_CONTEXT.getExpressionSuffix();
    private char prefix = '#';
    private char suffix = '(';

    private static Set<Character> supportChars;


    public LogRecordOperationSource() {
        supportChars = new HashSet<>();
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY_"+prefix+suffix+templatePrefix+templateSuffix;
        for (char c : str.toCharArray()) {
            supportChars.add(c);
        }
    }

    public List<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass){
        LogRecord logRecord = method.getAnnotation(LogRecord.class);
        List<LogRecordOps> ops = new ArrayList<>(DEFAULT_OPS_SIZE);
        if(!logRecord.success().isEmpty()){
            LogRecordOps op = new LogRecordOps("success", logRecord.success());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.bizNo().isEmpty()){
            LogRecordOps op = new LogRecordOps("bizNo", logRecord.bizNo());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.operator().isEmpty()){
            LogRecordOps op = new LogRecordOps("operator", logRecord.operator());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.category().isEmpty()){
            LogRecordOps op = new LogRecordOps("category", logRecord.category());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.detail().isEmpty()){
            LogRecordOps op = new LogRecordOps("detail", logRecord.detail());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.condition().isEmpty()){
            LogRecordOps op = new LogRecordOps("condition", logRecord.condition());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.fail().isEmpty()){
            LogRecordOps op = new LogRecordOps("fail", logRecord.fail());
            fillTemplated(op);
            ops.add(op);
        }
        return ops;
    }

    private void fillTemplated(LogRecordOps op){
        String opValue = op.getValue();
        boolean contain = opValue.contains(templatePrefix);
        if(contain){
            op.setTemplate(true);
            List<String> functionNames = parseAllTemplateMethods(opValue);
            op.setFunctionNames(functionNames);
            op.setBeforeExecute(functionNames.size() > 0);
        }else{
            op.setTemplate(false);
        }
    }


    /** 解析自定义函数 */
    private List<String> parseAllTemplateMethods(String template){
        if(template == null || template.isEmpty()){
            return Collections.emptyList();
        }
        List<String> methods = new ArrayList<>();
        StringBuilder sb = new StringBuilder(128);
        int len = template.length();
        char start = template.charAt(0);
        for(int end = 0; end < len; end++){
            char cur = template.charAt(end);
            if(cur == prefix || !supportChars.contains(cur)){
                start = cur;
                sb.delete(0, sb.length());
                continue;
            }
            sb.append(cur);
            if(start == prefix && cur == suffix){
                int sbLen = sb.length();
                if(sbLen > 1){
                    String substring = sb.substring(0, sbLen - 1);
                    methods.add(substring);
                }
                sb.delete(0, sbLen);
            }
        }
        return methods;
    }

//    public static void main(String[] args) {
//        String str = "res : #calc(#name,'asdf'，,#user.isAdmin(),#())------++++))))((()))#(#(  #isAdmin(#currentUser())";
//        List<String> strings = parseAllTemplateMethods(str);
//        System.out.println(strings);
//    }

}
