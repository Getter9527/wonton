package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.node.expression.Expr;

/**
 * 赋值语句
 */
public class AssignmentStmt extends Stmt {

    private final Token identifier;
    private final Expr value;

    public AssignmentStmt(final Token identifier, final Expr value) {
        this.identifier = identifier;
        this.value = value;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public Expr getValue() {
        return value;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth));
        builder.append("AssignmentStmt\n");
        String identifierFormat = indent(depth + 1) + "Identifier(" + identifier.getLexeme() + ")" + "\n";
        builder.append(identifierFormat);
        String valueFormat = "null";
        if (value != null) {
            valueFormat = value.pretty(depth + 1);
        }
        builder.append(valueFormat);
        return builder.toString();
    }
}
