package com.elltor.example.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {

    private String username;
    private String name;
    private Integer age;
    private String sex;

}
