package com.elltor.oplog.service.impl;

import com.elltor.oplog.service.IOperatorGetService;
import com.elltor.oplog.entity.Operator;

public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    private Operator operator;

    public DefaultOperatorGetServiceImpl() {
        operator = new Operator("", "");
    }

    @Override
    public Operator getUser() {
        return operator;
    }

}
