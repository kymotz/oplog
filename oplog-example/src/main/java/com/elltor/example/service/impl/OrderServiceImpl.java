package com.elltor.example.service.impl;

import com.elltor.example.entity.Order;
import com.elltor.example.service.IOrderService;
import com.elltor.oplog.annotation.LogRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements IOrderService {

//    @LogRecord(success = "【2】创建订单成功了，订单号为：{#order.id}", bizNo = "【2】{#order.id}",
//            fail = "【2】失败订单号为: {#order.id}", operator = "【2】{#order.name}", category = "【2】ORDER_LOG")
    @Override
    public void insert(Order order) throws Exception {
        order.setId(10001L);
        System.out.println("插入订单成功：" + order);
//        throw new Exception("主动抛异常");
    }

    @Override
    public Order getOrderById(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setName("男士卫衣一件");
        order.setAddress("北京市海淀区西二旗安宁佳园1001");
        order.setUserid("uid123455");
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}
