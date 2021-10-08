package com.example.demo.service;

import com.example.demo.entity.Order;
import org.apache.catalina.User;

public interface IOrderService {
    void insert(Order order);
    Order getOrderById(Long id);
}
