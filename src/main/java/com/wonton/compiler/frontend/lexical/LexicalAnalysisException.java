package com.wonton.compiler.frontend.lexical;

public class LexicalAnalysisException extends RuntimeException {

    private final Position position;

    public LexicalAnalysisException(String message) {
        super(message);
        this.position = new Position(-1, -1, -1);
    }

    public LexicalAnalysisException(String message, Position position) {
        super(String.format("[行 %d 列 %d:%d] %s", position.getLine(), position.getStart(), position.getEnd(), message));
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
