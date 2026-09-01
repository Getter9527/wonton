package com.wonton.syntax.node.statement;

import com.wonton.syntax.node.expression.Expr;

import java.util.List;

public class FunctionCallExpr extends Expr {

    /**
     * 调用的函数
     * 设计为 Expr 而非 Token，这样以后 f(1)(2)、(if ... f)(x) 这类高阶玩法都能支持
     */
    private final Expr callee;
    private final List<Expr> args;

    public FunctionCallExpr(final Expr callee, final List<Expr> args) {
        this.callee = callee;
        this.args = args;
    }

    public Expr getCallee() {
        return callee;
    }

    public List<Expr> getArgs() {
        return args;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth)).append("FunctionCallExpr\n");
        builder.append(indent(depth+1))
                .append("Name: ")
                .append(getCallee())
                .append("\n");
        builder.append(indent(depth+1)).append("Args: ");
        if (getArgs().isEmpty()) {
            builder.append("()");
        } else {
            builder.append("\n");
            for (int i = 0; i < getArgs().size(); i++) {
                builder.append(getArgs().get(i).pretty(depth + 2));
                if (i < getArgs().size()-1) {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }
}
