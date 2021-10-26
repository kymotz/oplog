package com.elltor.example.service.impl;

import com.elltor.example.entity.Order;
import com.elltor.example.service.IOrderService;
import com.elltor.oplog.annotation.LogRecord;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements IOrderService {

    @LogRecord(success = "{#userDetail(#order.name)} 创建了订单成功，订单号为：{#order.id}", bizNo = "{#order.id}",
            fail = "返回信息 订单id为: {#order.id}, 失败信息 errMsg : {#_errMsg}", category = "ORDER_LOG",
//            detail = "返回值 msg : {#_ret.msg} 状态码为: {#_ret.status} 返回数据: {#_ret.data}",
            condition = "{#calc(#order.id)}"
    )
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
        return order;
    }
}
