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
    public String pretty(int depth) {
        return indent(depth) + "DecimalExpr(" + getValue() + ")";
    }
}
