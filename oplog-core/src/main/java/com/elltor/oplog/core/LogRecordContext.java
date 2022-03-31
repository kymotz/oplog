package com.elltor.oplog.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 日志上线文对象。对于每个加日志注解的方法都会生成 Stack 中的一个栈帧。
 */
public class LogRecordContext {

    private static final Logger log = LoggerFactory.getLogger(LogRecordContext.class);

    /**
     * 具备继承特性线程局部变量，子线程继承父线程的线程局部变量
     */
    private static final InheritableThreadLocal<Stack<Map<String, Object>>> LOCAL_VALUABLES;

    // init, thread safe.
    static {
        LOCAL_VALUABLES = new InheritableThreadLocal<>() {
            @Override
            protected Stack<Map<String, Object>> initialValue() {
                return new Stack<>();
            }

            @Override
            protected Stack<Map<String, Object>> childValue(Stack<Map<String, Object>> parentValue) {
                Stack<Map<String, Object>> copiedStack = new Stack<>();
                // 仅共享父线程方法上下文中的变量
                if (!parentValue.isEmpty()) {
                    copiedStack.push(parentValue.peek());
                }
                return copiedStack;
            }
        };
    }

    public static Map<String, Object> getVariables() {
        final Stack<Map<String, Object>> stack = LOCAL_VALUABLES.get();
        if (stack.isEmpty()) {
            HashMap<String, Object> firstVariablePool = new HashMap<>();
            stack.push(firstVariablePool);
        }
        return stack.peek();
    }

    /**
     * 入栈一个 map。在 @LogRecord 嵌套使用时，即将入栈的 map 包含栈顶 map 的数据。
     */
     public static void putSpan() {
        if (LOCAL_VALUABLES.get().isEmpty()) {
            LOCAL_VALUABLES.get().push(new HashMap<>());
        } else {
            // 内层方法共享外层方法 Context Variables
            Map<String, Object> copiedMap = new HashMap<>(LOCAL_VALUABLES.get().peek());
            LOCAL_VALUABLES.get().push(copiedMap);
        }
    }

    /**
     * 结束一个方法清除栈顶的map
     */
    public static void clear() {
        Stack<Map<String, Object>> stack = LOCAL_VALUABLES.get();
        if (stack.isEmpty()) {
            log.info("Variable Stack is empty, without 'pop'.");
            return;
        }
        stack.pop();
    }

    /**
     * 在上线文中放入变量
     *
     * @param key   K
     * @param obj   V
     */
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
