# oplog

A util for operating log. Easy to use.

## Features

1、**Easy to use.** Provide `starter`, and auto-configuration by annotation.

2、**Flexibility.** Provide template to processing log, you can customize functions or expression, and provide log record control at the annotation level.

3、**Extensibility.** Provide custom functions, custom persistence operations, and custom log record operator.

4、**Performance.** Use asynchronous to process log persistence, and use caching to improve performance.

<br>

**Function Structure**

![业务逻辑](https://oss.elltor.com/uploads/2021/bde9c178c76e131cefae3e7d7fcf428993663_1635345437328.png)

## Usage

### Preface

See `oplog-example` module. This is a guide project, and you can learn some best practices through it.

Start the application, then click url `http://localhost:8080/swagger-ui/index.html`  visit swagger and try the function。

### How to use

Add `@Log Record` annotation to the method you need to log.

> The return value and parameters of the method constitute the context of the log template.

```java
    @LogRecord(success = "查询了用户, 用户id : {#userid}", category = "user")
    public Object getUserByUserid(String userid) {
        return new Object();
    }
```

### Custom log persistence

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

### Custom log operator

To implement interface `IOperatorGetService` and implement method.

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

### Custom parse function

To implement `IParseFunction` interface.

notice:
* custom method must be static. e.g. `userDetail` method.
* return value always `String` type.

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
    
    static String userDetail(String userid){
        User u = userDao.getUserByUserid(userid);
        return u.getName()+" "+u.getSex()+" "+u.getAge();
    }
}
```

### Special rule

The field in `LogRecord` :

* success : It's necessary field, can't be absent.
* condition : It's boolean value, but `String` type.
* operator : It's always be filled with current user from the method implement by `IOperatorGetService`. Or else the content you specify.
* fail : it's record fail method result.


## Technology

* SpEL(Spring Expression Language)
* Spring Message Service
* Java Annotation
* Spring Boot Auto-configuration

