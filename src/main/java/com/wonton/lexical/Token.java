package com.wonton.lexical;

import com.wonton.utils.StringUtils;

/**
 * 单词
 */
public class Token {

    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        // 字符串字面量含真实控制字符时需要转义渲染，其余类型原样输出
        Object displayLiteral = literal instanceof String str
                ? StringUtils.unescape(str)
                : literal;
        return String.format("类型: %-16s  词素: %-16s  字面量: %-12s  行: %-6s", type, lexeme, displayLiteral, line);
    }

}
