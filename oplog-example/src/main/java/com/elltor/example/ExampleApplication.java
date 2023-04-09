package com.elltor.example;

import com.elltor.oplog.annotation.EnableLogRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;

@SpringBootApplication
@EnableLogRecord(tenant = "com.elltor.biz", mode = AdviceMode.ASPECTJ)
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
        System.out.println("\n\t访问 Swagger 测试: http://localhost:8080/swagger-ui/index.html \n");
    }

}
