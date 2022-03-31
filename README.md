# oplog

一个便于使用的操作日志工具。

语言 : <span style="text-align:right;">[ <a href="https://github.com/elltor/oplog">英语</a> | 中文 ]</span>

## 功能

- 通过表达式解析生成美观的表达式，支持解析入参、自定义变量、返回值(_ret)、错误信息(_errMsg)
- 可以通过条件控制是否记录日志
- 提供自定义函数扩展
- 提供自定获取上下文用户扩展
- 支持嵌套、支持多线程使用情景

使用和效果预览：

```java
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
public Object getOrderById(@PathVariable("id") Long id)throws Exception{
        // 模拟成功失败
        Result success=new Result("success",200,orderService.getOrderById(id));
        Result fail=new Result("ERROR",500,orderService.getOrderById(id));

        // 随机成功失败
        return new Random().nextInt(10)>=5?success:fail;
        }
```

解析 @LogRecord 注解后的效果 :

```json
{
  "success": "订单详细信息: 【 id : 1001订单名称：男士卫衣一件 地址：北京市海淀区 】, 执行状态码:200",
  "fail": "获取订单失败 id: 1001, 执行状态码: 200",
  "operator": "zhangsan13",
  "bizNo": "1001",
  "category": "order",
  "detail": "",
  "condition": "true",
  "complete": true,
  "timestamp": 1648710916438
}
```

## 特性

- **便于使用。** 提供 `starter`, 通过注解开启功能并自动配置。

- **灵活。** 工具提供了**自定义函数**、具有表达式解析的日志模版和注解层面的日志持久化控制。

- **可扩展。** 提供了自定义函数、自定义日志操作用户、自定义持久化日志方式。

- **性能。** 通过异步和缓存的方式提升处理性能。

**业务逻辑架构**

![业务逻辑](https://oss.elltor.com/uploads/2021/bde9c178c76e131cefae3e7d7fcf428993663_1635345437328.png)

## 使用方法

### 前言

`oplog-example` 是一个演示模块，你可以通过这个项目快速了解工具的使用和功能。 启动项目后点击链接 `http://localhost:8080/swagger-ui/index.html` 访问 swagger
文档就可以尝试功能了。

### 1. 开始使用

开启日志记录功能 :

```java

@EnableLogRecord(tenant = "com.elltor.biz", mode = AdviceMode.PROXY)
@SpringBootApplication
public class Application {
    //.....
}
```

添加注解 `@LogRecord` 到你要记录日志的方法上。

> 被注解注释的方方法的入参和返回值将被作为日志模版的上下文。
>

```java
    @LogRecord(success = "查询了用户, 用户id : {#userid}", category = "user")
public Object getUserByUserid(String userid){
        return new Object();
        }
```

好了，到这里你就可以使用基本的功能了。

### 2. 自定义日志持久化方式

基础父类 `AbstractLogRecordService` 并且实现持久化方法 `record(Record record)` 。

```java

@Component
@Slf4j
public class PersistenceLogServiceImpl extends AbstractLogRecordService {
    @Override
    public void record(Record record) {
        log.info("example 包中 : {}", record);
    }
}
```

### 3. 自定义日志操作用户

实现接口 `IOperatorGetService` :

```java

@Component
public class LogRecordOperatorGetImpl implements IOperatorGetService {
    @Override
    public Operator getUser() {
        User user = ContextHolder.currentUser();
        Operator op = new Operator();
        op.setUsername(user.getUsername());
        op.setName(user.getName());
        return op;
    }
}
```

### 4. 自定义解析函数

注意:

* 自定义的解析函数必须是静态方法. 如下面的 `userDetail` 方法。
* 返回值总是 `String` 类型的。

实现接口 `IParseFunction` :

```java

@Component
public class UserDetailFunction implements IParseFunction {

    @Component
    private UserDao userDao;

    @Override
    public Method functionMethod() {
        Method method = null;
        try {
            method = UserDetailFunction.class.getDeclaredMethod("userDetail", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    // custom function
    static String userDetail(String userid) {
        User u = userDao.getUserByUserid(userid);
        return u.getName() + " " + u.getSex() + " " + u.getAge();
    }
}
```

### 5. 使用约定

在注解 `LogRecord` 中的字段 :

* success : 成功方法模版，必填字段。
* condition : 是否记录日志。值为boolean值, 但类型为 `String`。
* operator : 总是通过实现 `IOperatorGetService` 接口的类获取或者由你指定。
* fail : 用来记录方法执行失败的模版文案。

## 使用技术

* SpEL(Spring Expression Language)
* Spring Message Service
* Java Annotation
* Spring Boot Auto-configuration

## 实现参考

> https://tech.meituan.com/2021/09/16/operational-logbook.html
