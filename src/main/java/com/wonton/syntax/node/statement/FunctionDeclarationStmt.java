package com.wonton.syntax.node.statement;

import com.wonton.lexical.Token;

import java.util.List;

public class FunctionDeclarationStmt extends Stmt {

    private final Token name;
    private final List<Token> params; // params是形参列表，args是实参列表
    private final BlockStmt body;

    public FunctionDeclarationStmt(final Token name, final List<Token> params, final BlockStmt body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public Token getName() {
        return name;
    }

    public List<Token> getParams() {
        return params;
    }

    public BlockStmt getBody() {
        return body;
    }

    @Override
    public String pretty(int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(depth)).append("FunctionDeclarationStmt\n");
        // 函数名称
        builder.append(indent(depth + 1)).append("Name: ").append(getName().getLexeme()).append("\n");
        // 参数信息
        builder.append(indent(depth + 1)).append("Params:");
        if (getParams().isEmpty()) {
            builder.append("None").append("\n");
        } else {
            builder.append("\n");
            for (Token param : getParams()) {
                builder.append(indent(depth + 2));
                builder.append("- ").append(param.getLexeme()).append("\n");
            }
        }
        builder.append(getBody().pretty(depth + 1));
        return builder.toString();
    }
}
