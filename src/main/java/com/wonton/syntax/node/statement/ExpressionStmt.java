package com.wonton.syntax.node.statement;

import com.wonton.syntax.node.expression.Expr;

public class ExpressionStmt extends Stmt {

    private final Expr expr;

    /**
     * @param expr 作为语句执行的表达式
     */
    public ExpressionStmt(final Expr expr) {
        this.expr = expr;
    }

    /**
     * 获取表达式
     *
     * @return 表达式节点
     */
    public Expr getExpr() {
        return expr;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth) + "ExpressionStmt\n" + expr.pretty(depth + 1);
    }
}
