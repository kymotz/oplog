package com.example.demo.core;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LogRecordContext  {

    private static final InheritableThreadLocal<Stack<Map<String, Object>>> localValuable;

    // init, thread safe.
    static {
        localValuable = new InheritableThreadLocal<>(){
            @Override
            protected Stack<Map<String, Object>> initialValue() {
                return new Stack<>();
            }
        };
    }

    public static Map<String, Object> getVariables() {
        final Stack<Map<String, Object>> stack = localValuable.get();
        if(stack.isEmpty()){
            log.error("variableMapStack is empty, but will do 'pop' operate");
            return new HashMap<>(0);
        }
        return stack.peek();
    }

    /**
     * 入栈一个map
     */
    public static void putEmptySpan(){
        localValuable.get().push(new HashMap<>());
    }

    /**
     * 结束一个方法清除栈顶的map
     */
    public static void clear(){
        Stack<Map<String, Object>> stack = localValuable.get();
        if(stack.isEmpty()){
            log.error("variableMapStack is empty, but will do 'pop' operate");
            return;
        }
        stack.pop();
    }

    public static void putVariable(String key, Object obj){
        Stack<Map<String, Object>> stack = localValuable.get();
        checkAndCreateMap();
        stack.peek().put(key,obj);
    }

    private static void checkAndCreateMap(){
        if (localValuable.get().isEmpty()) {
            localValuable.get().push(new HashMap<>());
        }
    }

}
