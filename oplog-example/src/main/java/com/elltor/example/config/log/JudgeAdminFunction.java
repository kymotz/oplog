package com.elltor.example.config.log;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class JudgeAdminFunction implements IParseFunction {

    @Override
    public Method functionMethod() {
        try {
            return JudgeAdminFunction.class.getDeclaredMethod("isAdmin", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String isAdmin(String uid){
        return String.valueOf(true);
    }
}
