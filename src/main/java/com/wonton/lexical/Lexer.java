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
                    addToken(TokenType.Integer);
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
                StringBuilder builder = new StringBuilder();
                while (true) {
                    char c = peek();
                    // 字符串结束标志
                    if (c == '\0') {
                        scanError("字符串未闭合，缺少结束的引号");
                    }
                    // 不允许直接写真实的换行符，应该用转义符来表示
                    // 宿主语言为了表示换行，需要用转义符来表示
                    if (c == '\n') {
                        scanError("字符串不能包含真实的换行符");
                    }
                    if (c == '"') {
                        break;
                    }
                    c = advance();
                    // 宿主语言中，为了表示1个斜杠，需要用2个斜杠的写法来表示
                    if (c == '\\') {
                        // 反斜杠开启转义序列，翻译为真实字符后写入字面量
                        builder.append(escape());
                    } else {
                        builder.append(c);
                    }
                }
                // 消费剩下的那个引号
                advance();
                // 字面量是转义解析后的结果，已不等于源码原文，必须显式传入
                addToken(TokenType.String, builder.toString());
            }
            // 标识符 和 所有关键字
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
                if(match('=')) addToken(TokenType.Equalx2);
                else addToken(TokenType.Equal);
            }
            else if(ch == '!') {
                if (match('=')) addToken(TokenType.NotEqual);
                else addToken(TokenType.Not);
            }
            else if(ch == '<') {
                if (match('=')) addToken(TokenType.LessEqual);
                else addToken(TokenType.Less);
            }
            else if(ch == '>') {
                if (match('=')) addToken(TokenType.GreaterEqual);
                else addToken(TokenType.Greater);
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
            else if(ch == '%') addToken(TokenType.Modulo);
            else if(ch == '^') addToken(TokenType.Caret);
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

    private void addToken(TokenType type, Object literal) {
        Token token = new Token(type, getLexeme(), literal, line);
        tokens.add(token);
    }

    // 获取当前词素
    private String getLexeme() {
        return source.substring(start, current);
    }

    // 获取当前字面量
    private Object getLiteral(TokenType type) {
        if (type == TokenType.Integer) {
            return Long.parseLong(getLexeme());
        }
        if (type == TokenType.Decimal) {
            return new BigDecimal(getLexeme());
        }
        if (type == TokenType.Boolean) {
            return Boolean.valueOf(getLexeme());
        }
        return null;
    }

    // 判断字符是否是数字
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // 判断字符是否是十六进制数字（0-9、a-f、A-F）
    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * 解析反斜杠之后的转义序列，返回转义后的真实字符。
     * 转义集对齐 C 系语言标准命名转义（不含已弃用的八进制转义）。
     */
    private char escape() {
        if (isAtEnd()) {
            // 在宿主语言里面 为了标识目标语言中的1个反斜杠，需要用2个反斜杠的写法来表示
            scanError("字符串意外结束：'\\' 后面缺少转义字符");
        }
        char escaped = advance();
        return switch (escaped) {
            case 'n' -> '\n';   // 换行
            case 't' -> '\t';   // 制表符
            case 'r' -> '\r';   // 回车
            case 'b' -> '\b';   // 退格
            case 'f' -> '\f';   // 换页
            case '0' -> '\0';   // 空字符
            case '\\' -> '\\';   // 反斜杠本身
            case '"' -> '"';    // 双引号本身
            case 'u' -> unicodeEscape();
            default -> {
                scanError("不支持的转义字符：\\" + escaped);
                yield '\0'; // 跳出switch表达式
            }
        };
    }

    /**
     * 解析 \\u????：读取4位十六进制数字，转换为对应的Unicode字符
     */
    private char unicodeEscape() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (isAtEnd()) {
                scanError("\\u 转义不完整：需要4位十六进制数字");
            }
            char c = advance();
            if (!isHexDigit(c)) {
                scanError("\\u 转义无效：'" + c + "' 不是十六进制数字");
            }
            hex.append(c);
        }
        return (char) Integer.parseInt(hex.toString(), 16);
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

    private void scanError(String message) {
        throw new RuntimeException("[行 " + line + "] 词法错误：" + message);
    }

}
