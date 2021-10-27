package com.elltor.example.config.log;

import com.elltor.example.entity.User;
import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class UserDetailFunction implements IParseFunction {
    @Override
    public Method functionMethod() {
        Method method = null;
        try{
            method = UserDetailFunction.class.getDeclaredMethod("userDetail", String.class);
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }
        return method;
    }

    static String userDetail(String userid){
        User u = new User();
        u.setUserid(userid);
        u.setUsername("zhangsan");
        u.setName("张三");
        u.setAge(18);
        u.setSex("男");
        return u.getName()+" "+u.getSex()+" "+u.getAge();
    }
}
