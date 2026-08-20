package com.wonton.syntax.node.expression;

import com.wonton.lexical.Token;

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
    public String toString() {
        return "UnaryExpr(operator=" + operator + ", operand=" + operand + ")";
    }

    @Override
    public String pretty(int depth) {
        return indent(depth)
                + "UnaryExpr(" + operator.getLexeme() + ")" + "\n"
                + operand.pretty(depth + 1);
    }
}
