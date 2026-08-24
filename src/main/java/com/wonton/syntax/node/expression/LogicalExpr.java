package com.wonton.syntax.node.expression;

import com.wonton.lexical.Token;

public class LogicalExpr extends Expr {

    private final Token operator;
    private final Expr left;
    private final Expr right;

    public LogicalExpr(Token operator, Expr left, Expr right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public Token getOperator() {
        return operator;
    }

    public Expr getLeft() {
        return left;
    }

    public Expr getRight() {
        return right;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "LogicalExpr(" + getOperator().getLexeme() + ")" + "\n"
                + getLeft().pretty(depth + 1) + "\n"
                + getRight().pretty(depth + 1);
    }
}
