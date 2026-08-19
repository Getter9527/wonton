package com.wonton.lexical;

/**
 * 单词
 */
public class Token {

    private final TokenType type;
    private final String lexeme;
    private final int line;

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return String.format("类型: %-16s  词素: %-16s  行: %-6s", type, lexeme, line);
    }

}
