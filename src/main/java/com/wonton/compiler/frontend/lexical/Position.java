package com.wonton.compiler.frontend.lexical;

public class Position {

    private final int line;
    private final int start;
    private final int end;

    public Position(int line, int start, int end) {
        this.line = line;
        this.start = start;
        this.end = end;
    }

    public int getLine() {
        return line;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("Position(line=%d, start=%d, end=%d)", line, start, end);
    }
}
