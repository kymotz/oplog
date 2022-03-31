package com.elltor.example.util;

import com.elltor.example.entity.User;

import java.util.Random;

public class ContextHolder {

    public static User currentUser() {
        int ran = new Random().nextInt(25) + 5;
        User user = new User();
        user.setUsername("zhangsan" + ran);
        user.setName("张三 " + ran);
        user.setAge(ran);
        return user;
    }

}
