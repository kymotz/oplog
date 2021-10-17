package com.example.demo.controller;

import com.example.demo.annotation.LogRecord;
import com.example.demo.entity.Order;
import com.example.demo.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags="订单管理")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private IOrderService orderService;

    @ApiOperation("获取订单")
    @GetMapping("/{id}")
    public Object getOrderById(@PathVariable("id") Long id) throws Exception{
        return orderService.getOrderById(id);
    }

    @LogRecord(success = "创建订单成功了，订单号为：{#order.id}", bizNo = "{#order.id}",
               fail = "创建订单失败", operator = "{#order.name}")
    @ApiOperation("插入订单")
    @ApiImplicitParam(name = "order", value = "订单", paramType = "body", dataType = "Order")
    @PostMapping
    public Object insertOrder(Order order){
        orderService.insert(order);
        return "OK";
    }
}
