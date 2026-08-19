package com.wonton.lexical;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 词法解析器
 */
public class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    // 每个被扫描词素的起始位置
    private int start = 0;
    // 下一个即将被处理的字符
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    // 核心代码：分词
    public List<Token> tokenize() {
        while (!isAtEnd()) {
            /*
             * 外层循环每次结束，都表示添加了一个token
             * 所以start每次都会指向下一个token开始的位置
             * 并且start 永远慢 current 一步
             */
            start = current;
            // ch 是上一个字符
            char ch = advance();
            // 注释
            if (ch == '#') {
                while (peek() != '\n' && peek() != '\0') {
                    advance();
                }
            }
            // 数字
            else if(isDigit(ch)) {
                while (isDigit(peek())) {
                    advance();
                }
                // 判断是否是小数
                if (match('.')) {
                    // 取小数部分
                    while (isDigit(peek())) {
                        advance();
                    }
                    addToken(TokenType.Decimal);
                } else {
                    addToken(TokenType.Int);
                }
            }
            // 空白符
            else if(Character.isWhitespace(ch)) {
                if(ch == ' ') continue; // 忽略空格
                else if(ch == '\n') line++; // 记录行号
                else throw new RuntimeException("代码中存在不支持的空白字符");
            }
            // 字符串
            else if(ch == '"') {
                while (peek() != '"') {
                    advance();
                }
                // 消费剩下的那个引号
                advance();
                addToken(TokenType.Str);
            }
            // 标识符
            else if(isIdentifierStart(ch)) {
                while (isIdentifierPart(peek())) {
                    advance();
                }
                // 在添加标识符之前，判断这个标识符是否属于关键字
                final String lexeme = getLexeme();
                if (Keywords.isKeyword(lexeme)) {
                    addToken(Keywords.getType(lexeme));
                } else {
                    addToken(TokenType.Identifier);
                }
            }
            else if(ch == '=') {
                if(match('=')) addToken(TokenType.EqualX2);
                else addToken(TokenType.Equal);
            }
            else if(ch == '!') {
                if (match('=')) addToken(TokenType.NotEqual);
                else addToken(TokenType.NOT);
            }
            else if(ch == '<') {
                if (match('=')) addToken(TokenType.LE);
                else addToken(TokenType.LT);
            }
            else if(ch == '>') {
                if (match('=')) addToken(TokenType.GE);
                else addToken(TokenType.GT);
            }
            // 单字符
            else if(ch == '+') addToken(TokenType.Plus);
            else if(ch == '-') addToken(TokenType.Minus);
            else if(ch == '*') addToken(TokenType.Star);
            else if(ch == '/') addToken(TokenType.Slash);
            else if(ch == '.') addToken(TokenType.Dot);
            else if(ch == ',') addToken(TokenType.Comma);
            else if(ch == ';') addToken(TokenType.Semicolon);
            else if(ch == '(') addToken(TokenType.LeftParen);
            else if(ch == ')') addToken(TokenType.RightParen);
            else if(ch == '[') addToken(TokenType.LeftBracket);
            else if(ch == ']') addToken(TokenType.RightBracket);
            else if(ch == '{') addToken(TokenType.LeftBrace);
            else if(ch == '}') addToken(TokenType.RightBrace);
            else {
                // TODO throw new RuntimeException("代码中存在不支持的字符，在什么什么附近");
                System.err.printf("警告：未知字符【%s】，需要完善语法支持", ch);
            }
        }
        return tokens;
    }

    private void addToken(TokenType type) {
        Token token = new Token(type, getLexeme(), getLiteral(type), line);
        tokens.add(token);
    }

    // 获取当前词素
    private String getLexeme() {
        return source.substring(start, current);
    }

    // 获取当前字面量
    private Object getLiteral(TokenType type) {
        if (type == TokenType.Str) {
            return source.substring(start + 1, current - 1);
        }
        if (type == TokenType.Int) {
            return Long.parseLong(getLexeme());
        }
        if (type == TokenType.Decimal) {
            return new BigDecimal(getLexeme());
        }
        if (type == TokenType.True) {
            return Boolean.TRUE;
        }
        if (type == TokenType.False) {
            return Boolean.FALSE;
        }
        return null;
    }

    // 判断字符是否是数字
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // 标识符首字母规则
    private boolean isIdentifierStart(char c) {
        return c == '_' || isAlpha(c);
    }

    // 标识符剩下的规则
    private boolean isIdentifierPart(char c) {
        return c == '_' || isAlphaOrDigit(c);
    }

    // 是否是26个字母表中的字符
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // 是否是26个字母表中的字符 或 数字
    private boolean isAlphaOrDigit(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // 查看当前指针的字符，但是不消费它
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // 拿到待消费字符并返回，向后推进
    private char advance() {
        char ch = source.charAt(current);
        current++;
        return ch;
    }

    // 匹配
    private boolean match(char target) {
        if (isAtEnd()) {
            return false;
        }
        if(peek() != target) {
            return false;
        }
        current++;
        return true;
    }

    // 是否已消费完所有字符
    private boolean isAtEnd() {
        return current >= source.length();
    }

}
