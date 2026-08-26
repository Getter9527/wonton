package com.wonton.syntax.node.statement;

import java.util.List;

public class BlockStmt extends Stmt {

    private final List<Stmt> stmts;

    public BlockStmt(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth)).append("Block\n");
        for (Stmt stmt : stmts) {
            builder.append(stmt.pretty(depth + 1)).append("\n");
        }
        return builder.toString();
    }
}
