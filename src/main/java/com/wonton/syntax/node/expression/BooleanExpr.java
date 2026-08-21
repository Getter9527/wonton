package com.wonton.syntax.node.expression;

public class BooleanExpr extends Expr {

    private final Boolean value;

    public BooleanExpr(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public String pretty(int depth) {
        return "BooleanExpr(" + getValue() + ")";
    }
}
