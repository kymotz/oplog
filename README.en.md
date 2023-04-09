# oplog

A util for operating log. Easy to use.

Language : <span style="text-align:right;">[ English | <a href="https://github.com/elltor/oplog">Chinese</a> ]</span>


## Features

- **Easy to use.** Provide `starter`, and auto-configuration by annotation.

- **Flexibility.** We provide log processing templates,  customized functions or expressions and the log record control at the annotation level.

- **Extensibility.** Provide custom functions, custom persistence operations, and custom log record operator.

- **Performance.** Use asynchronous to process log persistence, and use caching to improve performance.

**Function Structure**

![业务逻辑](https://oss.elltor.com/uploads/2021/bde9c178c76e131cefae3e7d7fcf428993663_1635345437328.png)

## Usage

### Preface

See `oplog-example` module. This is a guide, and you can learn some best practices through it.

Start the application, then click url `http://localhost:8080/swagger-ui/index.html` to visit swagger and try the function。

### 1. Start

Enable log record function :

```java
@EnableLogRecord(tenant = "com.elltor.biz", mode = AdviceMode.PROXY)
@SpringBootApplication
public class Application {
    //.....
}
```

Add `@LogRecord` annotation to the method you need to log. 

> The return value and parameters of the method constitute the context of the log template.

```java
    @LogRecord(success = "查询了用户, 用户id : {#userid}", category = "user")
    public Object getUserByUserid(String userid) {
        return new Object();
    }
```

OK, Here you can use the basic functions. 

### 2. Custom log persistence

To extend `AbstractLogRecordService` and implement `record(Record record)` method.

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

### 3. Custom log operator

To implement interface `IOperatorGetService` :

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

### 4. Custom parse function

notice:
* custom method must be static. e.g. `userDetail` method.
* return value always `String` type.

To implement interface `IParseFunction` :

```java
@Component
public class UserDetailFunction implements IParseFunction {
    
    @Component
    private UserDao userDao;
    
    @Override
    public Method functionMethod() {
        Method method = null;
        try{
            method = UserDetailFunction.class.getDeclaredMethod("userDetail", String.class);
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }
        return method;
    }
    // custom function
    static String userDetail(String userid){
        User u = userDao.getUserByUserid(userid);
        return u.getName()+" "+u.getSex()+" "+u.getAge();
    }
}
```

### 5. Special rule

The field in `LogRecord` annotation :

* success : It's a required field.
* condition : It's a boolean value, but `String` type.
* operator : It's always be filled with current user from the method implement by `IOperatorGetService`. Or else the content you specify.
* fail : It's the result when the method fails. 


## Technology

* SpEL(Spring Expression Language)
* Spring Message Service
* Java Annotation
* Spring Boot Auto-configuration

## Implement reference

> https://tech.meituan.com/2021/09/16/operational-logbook.html
