package com.wonton.compiler.frontend.syntax.node.expression;

import com.wonton.compiler.frontend.lexical.Token;

public class UnaryExpr extends Expr {

    private final Token operator; // 操作符
    private final Expr operand;   // 操作数

    public UnaryExpr(Token operator, Expr operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public Token getOperator() {
        return operator;
    }

    public Expr getOperand() {
        return operand;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "UnaryExpr(" + getOperator().getLexeme() + ")" + "\n"
                + getOperand().pretty(depth + 1);
    }
}
