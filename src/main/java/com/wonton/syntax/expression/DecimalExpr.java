package com.wonton.syntax.expression;

import java.math.BigDecimal;

public class DecimalExpr extends Expr {

    private final String value;
    private final BigDecimal realValue;

    public DecimalExpr(String value) {
        this.value = value;
        this.realValue = new BigDecimal(value);
    }

    public BigDecimal getValue() {
        return realValue;
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
