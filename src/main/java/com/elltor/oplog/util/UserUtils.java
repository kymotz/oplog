package com.elltor.oplog.util;

import com.elltor.oplog.entity.Operator;

public class UserUtils {

    public static Operator getUser() {
        Operator operator = new Operator();
        operator.setUsername("m-user");
        operator.setUsername("m-username");
        return operator;
    }
}
