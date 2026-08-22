package com.wonton.interpreter;

import java.math.BigDecimal;

public class RuntimeValue {

    private final Type type;
    private final Object value;

    public RuntimeValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isNumbers() {
        return type == Type.Integer || type == Type.Decimal;
    }

    public boolean isInteger() {
        return type == Type.Integer;
    }

    public boolean isDecimal() {
        return type == Type.Decimal;
    }

    public boolean isBoolean() {
        return type == Type.Boolean;
    }

    public boolean isString() {
        return type == Type.String;
    }

    public boolean isNullType() {
        return type == Type.Null;
    }

    public static RuntimeValue of(Long value) {
        return new RuntimeValue(Type.Integer, value);
    }

    public static RuntimeValue of(Boolean value) {
        return new RuntimeValue(Type.Boolean, value);
    }

    public static RuntimeValue of(BigDecimal value) {
        return new RuntimeValue(Type.Decimal, value);
    }

    public static RuntimeValue of(String value) {
        return new RuntimeValue(Type.String, value);
    }

    @Override
    public String toString() {
        return String.format("RuntimeValue(type: %s, value: %s)", type, value);
    }

    public enum Type {
        Integer, Decimal, String, Boolean, Null;
    }

}
