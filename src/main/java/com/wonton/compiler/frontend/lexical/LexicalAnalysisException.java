package com.wonton.compiler.frontend.lexical;

public class LexicalAnalysisException extends RuntimeException {

    private final int line;
    private final int column;

    public LexicalAnalysisException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    public LexicalAnalysisException(String message, int line, int column) {
        super(String.format("[行 %d 列 %d] %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
