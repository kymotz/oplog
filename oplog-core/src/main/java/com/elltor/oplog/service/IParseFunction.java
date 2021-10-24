package com.elltor.oplog.service;

import java.lang.reflect.Method;

public interface IParseFunction {

    default boolean executeBefore() {
        return false;
    }

    Method functionMethod();
}