package com.example.demo.service.impl;

import com.example.demo.entity.Operator;
import com.example.demo.service.IOperatorGetService;
import com.example.demo.util.UserUtils;

import java.util.Optional;

public class DefaultOperatorGetServiceImpl implements IOperatorGetService {
    @Override
    public Operator getUser() {
        //UserUtils 是获取用户上下文的方法
        return Optional.ofNullable(UserUtils.getUser())
                .map(a -> new Operator(a.getName(), a.getUsername()))
                .orElseThrow(()->new IllegalArgumentException("user is null"));
    }
}
