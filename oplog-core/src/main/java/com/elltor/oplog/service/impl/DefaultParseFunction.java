package com.elltor.oplog.service.impl;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class DefaultParseFunction implements IParseFunction {

    @Override
    public Method functionMethod() {
        try {
            return DefaultParseFunction.class.getDeclaredMethod("doNothing", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    String doNothing(String value) {
        return value;
    }
}
