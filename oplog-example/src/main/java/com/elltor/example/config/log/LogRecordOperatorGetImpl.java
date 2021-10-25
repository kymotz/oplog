package com.elltor.example.config.log;

import com.elltor.example.entity.User;
import com.elltor.example.util.ContextHolder;
import com.elltor.oplog.entity.Operator;
import com.elltor.oplog.service.IOperatorGetService;
import org.springframework.stereotype.Component;

@Component
public class LogRecordOperatorGetImpl implements IOperatorGetService {

    @Override
    public Operator getUser() {
        final User user = ContextHolder.currentUser();
        Operator op = new Operator();
        op.setUsername(user.getUsername());
        op.setName(op.getName());
        return op;
    }

}
