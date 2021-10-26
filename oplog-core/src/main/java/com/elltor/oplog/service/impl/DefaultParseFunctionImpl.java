package com.elltor.oplog.service.impl;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class DefaultParseFunctionImpl implements IParseFunction {

    @Override
    public Method functionMethod() {
        try {
            return DefaultParseFunctionImpl.class.getDeclaredMethod("doNothing", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        // impossible return null, safe
        return null;
    }

    static String doNothing(String value) {
        return value;
    }

}
