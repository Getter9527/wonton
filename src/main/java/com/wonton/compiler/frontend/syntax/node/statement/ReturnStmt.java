package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.node.expression.Expr;

public class ReturnStmt extends Stmt {

    /**
     * 为什么需要keyword这个属性？
     * 原因：当没有返回值时，此时的keyword可以提供（行号等）元数据，方便处理报错信息
     */
    private final Token keyword;
    private final Expr value;


    public ReturnStmt(final Token keyword, final Expr value) {
        this.keyword = keyword;
        this.value = value;
    }

    public Token getKeyword() {
        return keyword;
    }

    public Expr getValue() {
        return value;
    }

    @Override
    public String pretty(int depth) {
        if (value == null) {
            return indent(depth) + "ReturnStmt(null)";
        }
        return indent(depth) + "ReturnStmt\n" + value.pretty(depth + 1);
    }
}
