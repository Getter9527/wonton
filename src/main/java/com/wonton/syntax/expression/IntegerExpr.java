package com.wonton.syntax.expression;

public class IntegerExpr extends Expr {

    private final String value;
    private final Integer realValue;

    public IntegerExpr(String value) {
        this.value = value;
        this.realValue = Integer.parseInt(value);
    }

    public Integer getValue() {
        return realValue;
    }

    @Override
    public String toString() {
        return "IntegerExpr(value=" + value + ")";
    }

    @Override
    protected String pretty(int depth) {
        return indent(depth) + "IntegerExpr(" + value + ")";
    }
}
