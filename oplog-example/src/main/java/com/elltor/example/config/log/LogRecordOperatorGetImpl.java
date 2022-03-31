package com.elltor.example.config.log;

import com.elltor.example.entity.User;
import com.elltor.example.util.ContextHolder;
import com.elltor.oplog.entity.Operator;
import com.elltor.oplog.service.IOperatorGetService;
import org.springframework.stereotype.Component;

/**
 * 扩展点，自动解析当前用户
 */
@Component
public class LogRecordOperatorGetImpl implements IOperatorGetService {
    @Override
    public Operator getUser() {
        // 上下文，实际项目中应该是 SecurityContextHolder
        User user = ContextHolder.currentUser();
        // 模拟获取的当前请求用户
        Operator op = new Operator();
        op.setUsername(user.getUsername());
        op.setName(user.getName());
        return op;
    }
}
