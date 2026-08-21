package com.wonton.syntax.node.expression;

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
                + "BinaryExpr(" + getOperator().getLexeme() + ")" + "\n"
                + getLeft().pretty(depth + 1) + "\n"
                + getRight().pretty(depth + 1);
    }
}
