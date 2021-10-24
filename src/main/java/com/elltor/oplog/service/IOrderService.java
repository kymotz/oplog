package com.elltor.oplog.service;

import com.elltor.oplog.entity.Order;


public interface IOrderService {
    void insert(Order order);

    Order getOrderById(Long id);
}
