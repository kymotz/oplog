package com.elltor.example.controller;

import com.elltor.example.entity.Order;
import com.elltor.example.entity.Result;
import com.elltor.example.enums.LogType;
import com.elltor.example.service.IOrderService;
import com.elltor.oplog.annotation.LogRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private IOrderService orderService;

    /**
     * 记录用户查询订单
     */
    @LogRecord(
            success = "查询了订单: {# orderDetail (#id )}",
            fail = "获取订单失败，id ：{ #id}",
            bizNo = "{#id}",
            category = LogType.ORDER)
    @ApiOperation("获取订单")
    @GetMapping("/{id}")
    public Object getOrderById(@PathVariable("id") Long id) throws Exception {
        return new Result("success", 200, orderService.getOrderById(id));
    }

    @LogRecord(success = "创建订单成功了，订单号为：{#order.id}，订单标题：{#order.name}",
            bizNo = "{#order.id}",
            fail = "失败订单id为: {#order.id}, 失败信息 errMsg : {#_errMsg}",
            operator = "{#order.name}", category = "ORDER_LOG",
            detail = "方法返回值 msg : {#_ret.msg} 状态码为: {#_ret.status} 返回数据: {#_ret.data}",
            // 调用函数，当是管理员是记录日志
            condition = "{ #isAdmin(#order.userid)} "
    )
    @ApiOperation("插入订单-验证基本解析内容")
    @ApiImplicitParam(name = "order", value = "订单", paramType = "body", dataType = "Order")
    @PostMapping("/insert1")
    public Object insertOrder(Order order) throws Exception {
        long start = System.currentTimeMillis();
        orderService.insert(order);

        System.out.println("-------\n" + (System.currentTimeMillis() - start) + "\n---------");
        return new Result("success", 200, "nbnbnbnbn!!!");
    }

}
