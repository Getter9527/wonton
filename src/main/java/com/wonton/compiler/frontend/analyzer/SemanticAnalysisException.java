package com.wonton.compiler.frontend.analyzer;

/**
 * 语义分析异常（携带源码位置）
 */
public class SemanticAnalysisException extends RuntimeException {

    private final int line;

    public SemanticAnalysisException(String message) {
        super(message);
        this.line = -1;
    }

    public SemanticAnalysisException(String message, int line) {
        super("[行 " + line + "] 语义错误：" + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}