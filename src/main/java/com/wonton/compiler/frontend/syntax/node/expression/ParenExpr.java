package com.wonton.compiler.frontend.syntax.node.expression;

public class ParenExpr extends Expr {

    private final Expr expression;

    public ParenExpr(Expr expression) {
        this.expression = expression;
    }

    public Expr getExpression() {
        return expression;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "ParenExpr" + "\n"
                + getExpression().pretty(depth + 1);
    }
}
