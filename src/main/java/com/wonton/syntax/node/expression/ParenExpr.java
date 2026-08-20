package com.wonton.syntax.node.expression;

public class ParenExpr extends Expr {

    private final Expr expression;

    public ParenExpr(Expr expression) {
        this.expression = expression;
    }

    public Expr getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "ParenExpr(" + expression + ")";
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "ParenExpr" + "\n"
                + expression.pretty(depth + 1);
    }
}
