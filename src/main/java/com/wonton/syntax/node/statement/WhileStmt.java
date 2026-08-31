package com.wonton.syntax.node.statement;

import com.wonton.syntax.node.expression.Expr;

public class WhileStmt extends Stmt {

    private final Expr condition;
    private final BlockStmt whileBlock;

    public WhileStmt(Expr condition, BlockStmt whileBlock) {
        this.condition = condition;
        this.whileBlock = whileBlock;
    }

    public Expr getCondition() {
        return condition;
    }

    public BlockStmt getWhileBlock() {
        return whileBlock;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "WhileStmt\n"
                + getCondition().pretty(depth + 1) + "\n"
                + getWhileBlock().pretty(depth + 1);
    }
}
