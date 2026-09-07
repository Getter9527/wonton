package com.wonton.compiler.frontend.lexical;

import com.wonton.utils.StringUtils;

/**
 * 单词
 */
public class Token {

    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final Position position;

    public Token(final TokenType type, final String lexeme, final Object literal, final Position position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.position = position;
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

    public Position getPosition() {
        return position;
    }

    public int getLine() {
        return getPosition().getLine();
    }

    @Override
    public String toString() {
        // 字符串字面量含真实控制字符时需要转义渲染，其余类型原样输出
        Object displayLiteral = literal instanceof String str
                ? StringUtils.unescape(str)
                : literal;
        return String.format("Token(type=%s, lexeme=%s, literal=%s, position=%s)", type, lexeme, displayLiteral, position);
    }

}
