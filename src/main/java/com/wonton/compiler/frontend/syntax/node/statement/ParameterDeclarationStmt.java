package com.wonton.compiler.frontend.syntax.node.statement;

import com.wonton.compiler.frontend.lexical.Token;
import com.wonton.compiler.frontend.syntax.node.expression.Expr;

public class ParameterDeclarationStmt extends Stmt {

    private final Token name;
    private final Token type;
    private final Expr initializer;

    public ParameterDeclarationStmt(final Token name, final Token type, final Expr initializer) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    public Token getName() {
        return name;
    }

    public Token getType() {
        return type;
    }

    public Expr getInitializer() {
        return initializer;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth)).append("ParameterDeclarationStmt\n");

        String identifierFormat = indent(depth+1) + "Identifier(" + name.getLexeme() + ")" + "\n";
        builder.append(identifierFormat);

        String typeFormat = indent(depth+1) + "TypeAnnotation(" + type.getLexeme() + ")" + "\n";
        builder.append(typeFormat);

        String initializerFormat = indent(depth + 1) + "null";
        if (initializer != null) {
            initializerFormat = initializer.pretty(depth + 1);
        }
        builder.append(initializerFormat);
        return builder.toString();
    }
}
