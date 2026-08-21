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

    public <T> T getValue(Class<T> clazz) {
        // TODO 根据runtimeType进行类型检查？
        return clazz.cast(value);
    }
}
