package com.example.demo.aspect;

import com.example.demo.annotation.LogRecordAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
@Aspect
@Slf4j
public class LogRecordAspect {

    // 注解切入
    @Pointcut("@annotation(com.example.demo.annotation.LogRecordAnnotation)")
    public void pointCut(){

    }

//    @Pointcut("execution(* com.example.demo.service.impl.OrderServiceImpl.insert(..))")
//    public void pointCut(){}

//    @Before(value = "pointCut()")
//    public void beforeAdd() {
//        System.out.println("插入前...");
//    }

    /* After 在 AfterRunning 之后，之后添加的AfterRunning在已添加的AfterRunning之前 */

//    @After(value = "pointCut()")
//    public void afterAdd() {
//        System.out.println("插入后...1");
//    }
//
//    @AfterReturning(value = "pointCut()", returning = "ret")
//    public void afterAdd2(JoinPoint joinPoint, Object ret) {
//        System.out.println("插入后...2; return = " + ret);
//    }
//
//    @AfterReturning(pointcut = "pointCut()", returning = "ret")
//    public void afterAdd3(JoinPoint joinPoint, Object ret) {
//        System.out.println("插入后...3; return = " + ret);
//    }

    @Around(value = "pointCut()")
    public void aroundOperate(ProceedingJoinPoint point) {
        System.out.println("point.getArgs() = " + Arrays.toString(point.getArgs()));

        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        LogRecordAnnotation logRecord = method.getAnnotation(LogRecordAnnotation.class);

        try {
            point.proceed();
        } catch (Throwable e) {
            log.error("point.proceed() 执行错误");
        }
        doRecord(point);
    }

    private void doRecord(ProceedingJoinPoint point) {


    }

    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void afterThrowException(Exception e) {
        e.printStackTrace();
        System.out.println("报错了; e=" + e.getMessage());
    }
}
