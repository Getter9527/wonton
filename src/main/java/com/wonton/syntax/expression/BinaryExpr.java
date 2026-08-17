package com.wonton.syntax.expression;

import com.wonton.lexical.Token;

public class BinaryExpr extends Expr {

    private final Token operator;
    private final Expr left;
    private final Expr right;

    public BinaryExpr(Token operator, Expr left, Expr right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "BinaryExpr(operator=" + operator + ", left=" + left + ", right=" + right + ")";
    }

    @Override
    protected String pretty(int depth) {
        return indent(depth)
                + "BinaryExpr(" + operator.getLexeme() + ")" + "\n"
                + left.pretty(depth + 1) + "\n"
                + right.pretty(depth + 1);
    }
}
