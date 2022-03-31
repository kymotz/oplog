package com.elltor.example;

import com.elltor.oplog.annotation.EnableLogRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableLogRecord(tenant = "com.elltor.biz", mode = AdviceMode.ASPECTJ)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("\n\t访问 Swagger 测试: http://localhost:8080/swagger-ui/index.html \n");
    }

}
