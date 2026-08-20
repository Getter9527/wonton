package com.wonton.syntax.node.expression;

public class IntegerExpr extends Expr {

    private final Long value;

    public IntegerExpr(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
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
