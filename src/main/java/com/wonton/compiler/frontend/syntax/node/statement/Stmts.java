package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.syntax.node.Node;

import java.util.List;

public class Stmts extends Node {

    private final List<Stmt> stmts;

    public Stmts(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth))
                .append("Stmts\n");
        for (Stmt stmt : stmts) {
            builder.append(stmt.pretty(depth + 1));
            builder.append("\n");
        }
        return builder.toString();
    }
}
