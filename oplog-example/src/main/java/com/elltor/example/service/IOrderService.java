package com.elltor.example.service;

import com.elltor.example.entity.Order;


public interface IOrderService {
    void insert(Order order);

    Order getOrderById(Long id);
}
