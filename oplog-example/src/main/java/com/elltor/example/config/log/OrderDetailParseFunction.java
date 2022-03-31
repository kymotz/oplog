package com.elltor.example.config.log;

import com.elltor.oplog.service.IParseFunction;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 订单订单详情 自定义函数
 */
@Component
public class OrderDetailParseFunction implements IParseFunction {
    @Override
    public boolean executeBefore() {
        return true;
    }

    @Override
    public Method functionMethod() {
        Method method = null;
        try {
            method = OrderDetailParseFunction.class.getDeclaredMethod("orderDetail", Long.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    static String orderDetail(Long id) {
        return "【 id : " + id + "订单名称：男士卫衣一件 地址：北京市海淀区 】";
    }

}
