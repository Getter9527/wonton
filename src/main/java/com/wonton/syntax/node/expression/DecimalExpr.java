package com.wonton.syntax.node.expression;

import java.math.BigDecimal;

public class DecimalExpr extends Expr {

    private final BigDecimal value;

    public DecimalExpr(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DecimalExpr(value=" + value + ")";
    }

    @Override
    protected String pretty(int depth) {
        return indent(depth) + "DecimalExpr(" + value + ")";
    }
}
