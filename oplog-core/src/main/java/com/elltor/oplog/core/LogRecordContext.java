package com.elltor.oplog.core;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LogRecordContext {

    private static final InheritableThreadLocal<Stack<Map<String, Object>>> LOCAL_VALUABLES;

    // init, thread safe.
    static {
        LOCAL_VALUABLES = new InheritableThreadLocal<>() {
            @Override
            protected Stack<Map<String, Object>> initialValue() {
                return new Stack<>();
            }
        };
    }

    public static Map<String, Object> getVariables() {
        final Stack<Map<String, Object>> stack = LOCAL_VALUABLES.get();
        if (stack.isEmpty()) {
            log.error("variableMapStack is empty, but will do 'pop' operate");
            return new HashMap<>(0);
        }
        return stack.peek();
    }

    /**
     * 入栈一个map
     */
    public static void putEmptySpan() {
        LOCAL_VALUABLES.get().push(new HashMap<>());
    }

    /**
     * 结束一个方法清除栈顶的map
     */
    public static void clear() {
        Stack<Map<String, Object>> stack = LOCAL_VALUABLES.get();
        if (stack.isEmpty()) {
            log.error("VariableMapStack is empty, but will do 'pop' operator");
            return;
        }
        stack.pop();
    }

    public static void putVariable(String key, Object obj) {
        Stack<Map<String, Object>> stack = LOCAL_VALUABLES.get();
        checkAndCreateMap();
        stack.peek().put(key, obj);
    }

    private static void checkAndCreateMap() {
        if (LOCAL_VALUABLES.get().isEmpty()) {
            LOCAL_VALUABLES.get().push(new HashMap<>());
        }
    }

}
