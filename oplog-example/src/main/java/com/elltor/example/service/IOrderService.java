package com.elltor.example.service;

import com.elltor.example.entity.Order;


public interface IOrderService {

    void insert(Order order) throws Exception;

    Order getOrderById(Long id);

}
