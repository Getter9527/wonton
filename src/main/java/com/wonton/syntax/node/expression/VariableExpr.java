package com.wonton.syntax.node.expression;

import com.wonton.lexical.Token;

public class VariableExpr extends Expr {

    private final Token identifier;

    public VariableExpr(final Token identifier) {
        this.identifier = identifier;
    }

    public Token getIdentifier() {
        return identifier;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth) + "VariableExpr(" + identifier.getLexeme() + ")";
    }
}
