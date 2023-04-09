package com.elltor.example.config.log;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 解析用户 自定义方法
 */
//@Component
public class UserDetailParseFunction implements IParseFunction {

    @Override
    public Method functionMethod() {
        try {
            return UserDetailParseFunction.class.getDeclaredMethod("userDetail", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String userDetail(String username){
        return username + " 男" + " 18";
    }
}
