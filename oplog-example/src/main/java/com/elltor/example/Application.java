package com.elltor.example;

import com.elltor.oplog.annotation.EnableLogRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;

@SpringBootApplication
@EnableLogRecord(tenant = "com.elltor.biz", mode = AdviceMode.ASPECTJ)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("\n\tVisit Swagger UI : http://localhost:8080/swagger-ui/index.html \n");
    }

}
