package com.wonton.compiler.frontend.syntax.node;

import com.wonton.compiler.frontend.syntax.node.statement.Stmt;

import java.util.List;

public class Program extends Node {

    private final List<Stmt> stmts;

    public Program(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth)).append("Program\n");
        for (Stmt stmt : stmts) {
            builder.append(stmt.pretty(depth + 1));
            builder.append("\n");
        }
        return builder.toString();
    }
}
