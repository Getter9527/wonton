package com.wonton.syntax.node.expression;

public class NullExpr extends Expr {

    @Override
    public String pretty(int depth) {
        return indent(depth) + "NullExpr(null)";
    }
}
