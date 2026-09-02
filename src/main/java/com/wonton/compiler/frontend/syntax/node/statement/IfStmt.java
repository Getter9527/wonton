package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.syntax.node.expression.Expr;

public class IfStmt extends Stmt {

    private final Expr condition;
    private final BlockStmt ifBlock;
    private final BlockStmt elseBlock;

    public IfStmt(Expr condition, BlockStmt ifBlock, BlockStmt elseBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    public Expr getCondition() {
        return condition;
    }

    public BlockStmt getIfBlock() {
        return ifBlock;
    }

    public BlockStmt getElseBlock() {
        return elseBlock;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth));
        builder.append("IfStmt\n");
        builder.append(condition.pretty(depth + 1)).append("\n");
        builder.append(ifBlock.pretty(depth + 1));
        if (elseBlock != null) {
            builder.append(elseBlock.pretty(depth + 1));
        }
        return builder.toString();
    }
}


