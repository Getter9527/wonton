package com.wonton.interpreter;

public class Value {

    private final RuntimeType runtimeType;
    private final Object value;

    public Value(RuntimeType runtimeType, Object value) {
        this.runtimeType = runtimeType;
        this.value = value;
    }

    public RuntimeType getRuntimeType() {
        return runtimeType;
    }

    public Object getValue() {
        return value;
    }
}
