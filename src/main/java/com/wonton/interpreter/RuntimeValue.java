package com.wonton.interpreter;

public class RuntimeValue {

    private final RuntimeType runtimeType;
    private final Object value;

    public RuntimeValue(RuntimeType runtimeType, Object value) {
        this.runtimeType = runtimeType;
        this.value = value;
    }

    public RuntimeType getRuntimeType() {
        return runtimeType;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Value(runtimeType: %s, value: %s)", runtimeType, value);
    }
}
