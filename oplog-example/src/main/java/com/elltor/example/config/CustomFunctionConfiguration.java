package com.elltor.example.config;

import com.elltor.example.config.Functions.CustomFunctions;
import com.elltor.oplog.service.IParseFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class CustomFunctionConfiguration {

    @Bean
    public IParseFunction judgingAdminFunction() {
        try {
            Method method = CustomFunctions.class.getDeclaredMethod("isAdmin", Integer.class);
            return IParseFunction.fromLambda(method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public IParseFunction userDetailParseFunction() {
        try {
            Method method = CustomFunctions.class.getDeclaredMethod("userDetail", String.class);
            return IParseFunction.fromLambda(method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public IParseFunction orderDetailParseFunction() {
        try {
            Method method = CustomFunctions.class.getDeclaredMethod("orderDetail", Long.class);
            return IParseFunction.fromLambda(method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
