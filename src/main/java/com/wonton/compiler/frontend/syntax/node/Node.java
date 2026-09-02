package com.wonton.compiler.frontend.syntax.node;

public abstract class Node {

    /**
     * 缩进
     * @param depth 缩进深度
     */
    protected String indent(int depth) {
        // 4个空格 x depth
        return "    ".repeat(depth);
    }

    public abstract String pretty(int depth);

    public String toPrettyString() {
        return pretty(0);
    }

    @Override
    public String toString() {
        return toPrettyString();
    }

}
