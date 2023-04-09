package com.elltor.example.service.impl;

import com.elltor.example.entity.Order;
import com.elltor.example.service.IOrderService;
import com.elltor.oplog.annotation.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    public Order getOrderById(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setName("韩版加绒卫衣 XXXXL 码");
        order.setAddress("北京市,海淀区");
        return order;
    }

    // 嵌套注解
    @LogRecord(success = "{#userDetail(#order.name)} 创建了订单成功，订单号为：{#order.id}",
            bizNo = "{#order.id}",
            fail = "返回信息 订单id为: {#order.id}, 失败信息 errMsg : {#_errMsg}",
            category = "ORDER_LOG",
            condition = "{(new Integer(#calc(#order.id))>100)+''}")
    @Override
    public void insert(Order order) throws Exception {
        log.info("成功创建一个订单, 订单id = {}", order.getId());
    }

}
