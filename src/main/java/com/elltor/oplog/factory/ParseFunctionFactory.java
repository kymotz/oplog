package com.elltor.oplog.factory;

import com.elltor.oplog.service.IParseFunction;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFunctionFactory {

    private Map<String, IParseFunction> allFunctionMap;

    public ParseFunctionFactory(List<IParseFunction> parseFunctions) {
        if (parseFunctions == null || parseFunctions.isEmpty()) {
            allFunctionMap = Collections.emptyMap();
            return;
        }
        allFunctionMap = new HashMap<>();
        for (IParseFunction parseFunction : parseFunctions) {
            Method fm = parseFunction.functionMethod();
            if (fm == null) {
                continue;
            }
            allFunctionMap.put(fm.getName(), parseFunction);
        }
    }

    public IParseFunction getFunction(String functionName) {
        return allFunctionMap.get(functionName);
    }

    public boolean isBeforeFunction(String functionName) {
        return allFunctionMap.get(functionName) != null && allFunctionMap.get(functionName).executeBefore();
    }

}
