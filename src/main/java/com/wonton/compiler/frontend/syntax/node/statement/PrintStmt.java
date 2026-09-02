package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.syntax.node.expression.Expr;

public class PrintStmt extends Stmt {

    private final Expr value;

    public PrintStmt(Expr expr) {
        this.value = expr;
    }

    public Expr getValue() {
        return value;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "PrintStmt\n"
                + value.pretty(depth + 1);
    }
}
