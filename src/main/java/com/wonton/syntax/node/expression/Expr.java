package com.wonton.syntax.node.expression;

import com.wonton.syntax.node.Node;

public abstract class Expr extends Node {

    protected abstract String pretty(int depth);

    /**
     * 缩进
     * @param depth 缩进深度
     */
    protected String indent(int depth) {
        // 4个空格 x depth
        return "    ".repeat(depth);
    }

    public String toPrettyString() {
        return pretty(0);
    }
}
