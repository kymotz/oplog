package com.elltor.oplog.service.impl;

import com.elltor.oplog.service.IOperatorGetService;
import com.elltor.oplog.entity.Operator;
import com.elltor.oplog.util.UserUtils;

import java.util.Optional;

public class DefaultOperatorGetServiceImpl implements IOperatorGetService {
    @Override
    public Operator getUser() {
        //UserUtils 是获取用户上下文的方法
        return Optional.ofNullable(UserUtils.getUser())
                .map(a -> new Operator(a.getName(), a.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("user is null"));
    }
}
