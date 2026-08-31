package com.wonton.syntax.node.expression;

import com.wonton.utils.StringUtils;

public class StringExpr extends Expr {

    private final String value;

    public StringExpr(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String pretty(int depth) {
        return indent(depth) + "StringExpr(" + StringUtils.unescape(getValue()) + ")";
    }
}
