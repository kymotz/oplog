package com.example.demo.service.impl;

import com.example.demo.annotation.LogRecord;
import com.example.demo.entity.Order;
import com.example.demo.service.IOrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    public void insert(Order order) {
        System.out.println("插入订单成功：" + order);
//        throw new RuntimeException("主动抛异常");
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
