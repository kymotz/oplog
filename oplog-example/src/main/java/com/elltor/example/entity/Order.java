package com.elltor.example.entity;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class Order {
    private Long id;
    private String name;
    private String address;
    private String userid;
    private LocalDateTime createTime;
}
