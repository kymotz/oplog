package com.elltor.example.config.log;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 自定义函数 判断用户是否为管理员
 */
//@Component
public class JudgingAdminFunction implements IParseFunction {

    @Override
    public Method functionMethod() {
        try {
            return JudgingAdminFunction.class.getDeclaredMethod("isAdmin", Integer.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 偶数是管理员, 奇数非管理员
     */
    static String isAdmin(Integer uid){
        if (uid == null) {
            return "false";
        }
        return uid % 2 == 0 ? "true" : "false";
    }
}
