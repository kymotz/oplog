package com.elltor.oplog.service.impl;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CalcParseFunction implements IParseFunction {

    private static Method cache;

    @Override
    public Method functionMethod() {
        if (cache == null) {
            try {
                cache = CalcParseFunction.class.getDeclaredMethod("calc", String.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return cache;
    }

    private static String calc(String val) {
        return (Integer.parseInt(val) * 2) + "";
    }

}
