package com.elltor.example.controller;

import com.elltor.example.config.log.JudgingAdminFunction;
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
import java.util.Random;

/**
 * 模拟记录订单信息日志 - 基础演示
 *
 * 特殊演示见:
 * @see UserController
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private IOrderService orderService;

    /**
     * 示例 1 - 基本使用
     * 记录订单编号和返回值信息。
     *
     * 通过日志持久化实现类 {@link com.elltor.example.config.log.PersistenceLogServiceImpl } 打印结果
     * 通过 {@link com.elltor.example.config.log.LogRecordOperatorGetImpl } 自动获取操作用户
     * 通过自定义函数 {@link com.elltor.example.config.log.OrderDetailParseFunction } 解析订单详细信息
     */
    // 打印日志注解
    @LogRecord(
            // 必填，方法执行成功生成的模板（执行过程未捕获到异常）, orderDetail为自定义函数名
            success = "订单详细信息: {#orderDetail(#id)}, 执行状态码:{#_ret.status}",
            // 可选，执行失败的模板（捕捉到错误）
            fail = "获取订单失败 id: {#id}, 执行状态码: {#_ret.status}",
            // 可选，业务id
            bizNo = "{#id}",
            // 可选，日志的分类
            category = LogType.ORDER)
    @ApiOperation("获取订单信息")
    @GetMapping("/{id}")
    public Object getOrderById(@PathVariable("id") Long id) throws Exception {
        // 模拟成功失败
        Result success = new Result("success", 200, orderService.getOrderById(id));
        Result fail = new Result("ERROR", 500, orderService.getOrderById(id));

        // 随机成功失败
        return new Random().nextInt(10) >= 5 ? success : fail;
    }

    /**
     * 示例 2 - 带条件的日志收集
     * 根据 condition 为 "true" 或 "false" 判断是否记录日志
     *
     * 通过自定义函数 {@link JudgingAdminFunction} 来判断是否为管理员, 以决定是否记录日志 
     */
    @LogRecord(
            // 必填，方法执行成功生成的模板（执行过程未捕获到异常）
            success = "订单id: {#order.id}",
            // 可选，执行失败的模板（捕捉到错误）
            fail = "获取订单失败, 失败信息: {#_errMsg}",
            // 可选，指定操作用户，覆盖自动获取
            operator = "{#order.optId}",
            // 可选，条件字段, 当是管理员是记录日志，默认为: "true" 记录所有日志
            condition = "{#isAdmin(#order.optId)} ", category = LogType.ORDER)
    @ApiOperation("插入订单-验证条件解析内容")
    @ApiImplicitParam(name = "order", value = "订单", paramType = "body", dataType = "Order")
    @PostMapping("/insert")
    public Object insertOrder(Order order) throws Exception {
        orderService.insert(order);
        return new Result("success", 200, "");
    }

}
