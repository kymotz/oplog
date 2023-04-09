package com.elltor.example.config;

import com.elltor.example.config.functions.CustomFunctions;
import com.elltor.oplog.service.IParseFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class CustomFunctionConfiguration {

    private IParseFunction createParseFunction(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return IParseFunction.fromLambda(method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public IParseFunction judgingAdminFunction() {
        return createParseFunction(CustomFunctions.class, "isAdmin", Integer.class);
    }

    @Bean
    public IParseFunction userDetailParseFunction() {
        return createParseFunction(CustomFunctions.class, "userDetail", String.class);
    }

    @Bean
    public IParseFunction getOldOrderParseFunction() {
        return createParseFunction(CustomFunctions.class, "orderDetail", Long.class);
    }
}
