package com.wonton.interpreter;

import java.math.BigDecimal;

public class RuntimeValue {

    private final Type type;
    private final Object value;

    public RuntimeValue(Type type, Object value) {
        this.type = type;
        this.value = value;
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

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("RuntimeValue(type: %s, value: %s)", type, value);
    }

    public static enum Type {
        Integer, Decimal, String, Boolean, Null;

        public boolean isNumeric() {
            return this == Integer || this == Decimal;
        }

        public boolean isInteger() {
            return this == Integer;
        }

        public boolean isDecimal() {
            return this == Decimal;
        }

        public boolean isBoolean() {
            return this == Boolean;
        }

        public boolean isString() {
            return this == String;
        }

        public boolean isNull() {
            return this == Null;
        }
    }

}
