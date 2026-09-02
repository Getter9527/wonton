package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.node.expression.Expr;

/**
 * 变量声明语句
 */
public class ConstantDeclarationStmt extends Stmt {

    private final Token identifier;
    private final Expr initializer;

    public ConstantDeclarationStmt(final Token identifier, final Expr initializer) {
        this.identifier = identifier;
        this.initializer = initializer;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public Expr getInitializer() {
        return initializer;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth));
        builder.append("ConstantDeclarationStmt\n");
        String identifierFormat = indent(depth+1) + "Identifier(" + identifier.getLexeme() + ")" + "\n";
        builder.append(identifierFormat);
        String initializerFormat = indent(depth + 1) + "null";
        if (initializer != null) {
            initializerFormat = initializer.pretty(depth + 1);
        }
        builder.append(initializerFormat);
        return builder.toString();
    }
}
