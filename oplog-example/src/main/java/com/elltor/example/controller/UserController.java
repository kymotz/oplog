package com.elltor.example.controller;

import com.elltor.example.entity.Result;
import com.elltor.example.entity.User;
import com.elltor.example.enums.LogType;
import com.elltor.oplog.annotation.LogRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 特殊功能演示
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 用户查询
     */
    @LogRecord(
            success = "查询了用户: {#orderDetail(#id)}",
            fail = "查询用户失败",
            bizNo = "{#userid}",
            // 对象不能想Java中直接转为字符串, 需要调用toString方法
            detail = "请求结果，msg：{#_ret.msg} status:{#_ret.status} data : {#_ret.data.toString()}",
            category = LogType.USER)
    @ApiOperation("获取订单")
    @GetMapping("/{userid}")
    public Object getUserByUserid(@PathVariable("userid") String userid) throws Exception {
        User u = new User();
        u.setUserid(userid);
        u.setUsername("zhangsan");
        u.setName("张三");
        u.setAge(18);
        u.setSex("男");
        return new Result("success", 200, u);
    }

}
