package com.wonton.interpreter;

public enum RuntimeType {
    Integer,
    Decimal,
    String,
    Boolean,
    Null;

    public static boolean isNumber(RuntimeType type) {
        return type == Integer || type == Decimal;
    }
}
