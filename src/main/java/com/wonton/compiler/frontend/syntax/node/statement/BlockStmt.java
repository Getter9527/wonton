package com.wonton.compiler.frontend.syntax.node.statement;

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
        builder.append(indent(depth)).append("Block").append("\n");
        if (getStmts().isEmpty()) {
            builder.append(indent(depth+1)).append("{}");
        }else {
            for (int i = 0; i < getStmts().size(); i++) {
                builder.append(getStmts().get(i).pretty(depth + 1));
                if (i < getStmts().size()-1) {
                    builder.append("\n");
                }
            }
        }

        return builder.toString();
    }
}
