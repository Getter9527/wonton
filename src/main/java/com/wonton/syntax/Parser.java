package com.wonton.syntax;

import com.wonton.lexical.Token;
import com.wonton.lexical.TokenType;
import com.wonton.logger.Logger;
import com.wonton.syntax.node.expression.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

/**
 * 语法解析器
 */
public class Parser {

    /**
     * 词法分析产生的 Token 序列，供语法解析器按序消费
     */
    private final List<Token> tokens;

    /**
     * 正在处理的 Token
     */
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    public Expr parse() {
        Expr ast = expr();
        return ast;
    }

    public Expr expr() {
        // <expr> ::= <term> (<op> <term>)*
        // <op>   ::= "+" | "-"
        // 先解析首个项（也是左操作数）
        Expr expr = term();
        // 实现左结合的加减运算
        while (matchAny(TokenType.Plus, TokenType.Minus)) {
            // 获取操作符（+、-）
            Token operator = previous();
            // 获取右操作数
            Expr right = term();
            // 构建二元表达式
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    public Expr term() {
        // <term> ::= <factor> (<op> <factor>)*
        // <op>   ::= "*" | "/"
        Expr expr = factor();
        while (matchAny(TokenType.Star, TokenType.Slash)) {
            // 获取操作符（*、/）
            Token operator = previous();
            // 获取右操作数
            Expr right = factor();
            // 构建二元表达式
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    public Expr factor() {
        // <factor> ::= <unary>
        return unary();
    }

    public Expr unary() {
        // <unary>  ::= <op> <unary> | <primary>
        // <op>     ::= "+" | "-" | "~"
        if (matchAny(TokenType.Plus, TokenType.Minus, TokenType.NOT)) {
            // 获取操作符（+、-、~）
            Token operator = previous();
            // 右侧的操作数本身可能也是一个一元表达式，所以这里递归
            Expr operand = unary();
            return new UnaryExpr(operator, operand);
        }
        // 当没有匹配到一元运算符时，不再递归
        return primary();
    }

    /**
     * 基本项，原子量
     * @description
     * <p>primary 是表达式的最小原子单元，不可再被运算符拆分的基本元素，或者是一个被括号包裹的完整子表达式。           </p>
     * <p>括号是 primary 的特殊形式，它的语义是"把子表达式打包成一个整体"，通过递归调用 expr() 解析这个内部的子表达式。</p>
     */
    public Expr primary() {
        // <primary> ::= <integer> | <decimal> | <boolean> | <string> | <paren>
        // <boolean>::= "true" | "false"
        // <paren>  ::= "(" <expr> ")"
        if (match(TokenType.Int)) {
            Long literal = (Long) previous().getLiteral();
            return new IntegerExpr(literal);
        }
        if (match(TokenType.Decimal)) {
            BigDecimal literal = (BigDecimal) previous().getLiteral();
            return new DecimalExpr(literal);
        }
        if (match(TokenType.Str)) {
            String literal = (String) previous().getLiteral();
            return new StringExpr(literal);
        }
        if (match(TokenType.Boolean)) {
            Boolean literal = (Boolean) previous().getLiteral();
            return new BooleanExpr(literal);
        }
        // 解析左括号
        if (match(TokenType.LeftParen)) {
            // 解析括号中的表达式
            Expr expression = expr();
            // 解析右括号，如果没有匹配到右括号，则抛出异常
            if (!match(TokenType.RightParen)) {
                parseError("表达式未闭合，缺少右括号", previous().getLine());
            }
            return new ParenExpr(expression);
        }
        return null;
    }

    /**
     * 获取当前正在处理的 Token
     */
    public Token peek() {
        return tokens.get(current);
    }

    /**
     * 获取上一个 Token信息
     */
    public Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * 推进 Token
     * @description 获取当前正在处理的 Token，并向后推进
     */
    public Token advance() {
        Token token = tokens.get(current);
        current++;
        return token;
    }

    /**
     * 匹配 Token
     * @description 匹配当前 Token 是否为指定类型，如果是则消费之；否则不消费
     */
    public boolean match(TokenType type) {
        if (current >= tokens.size()) {
            return false;
        }
        Token token = peek();
        if (token.getType() != type) {
            return false;
        }
        current++;
        return true;
    }

    /**
     * 匹配任意一个（或多个） Token
     * @description 如果有任意一个（或多个）Token被匹配上，则消费之；否则不消费
     */
    public boolean matchAny(TokenType ...types) {
        if (current >= tokens.size()) {
            return false;
        }
        Token token = peek();
        for (TokenType type : types) {
            if (token.getType() == type) {
                current++;
                return true;
            }
        }
        return false;
    }

    public boolean isCurrent(TokenType type) {
        // 防止获取下一个元素时，下坐标越界
        if (current >= tokens.size()) {
            return false;
        }
        Token token = peek();
        return token.getType() == type;
    }

    public Token expect(TokenType type) {
        // 防止获取下一个元素时，下坐标越界
        if (current >= tokens.size()) {
            Token token = previous();
            final String errMsg = MessageFormat.format("下坐标越界，错误发生在：{0}", token.getLexeme());
            parseError(errMsg, token.getLine());
        }
        // 不符合预期
        Token token = peek();
        if (token.getType() != type) {
            final String errMsg = MessageFormat.format("期望 {0} 但实际遇到 {1}", type, token.getType());
            parseError(errMsg, token.getLine());
        }
        // 符合预期
        return advance();
    }

    private void parseError(String message, int line) {
        Logger.error("[行 {1} 列 {2}] {0}", message, line, "3:10");
        // 一般性错误导致的退出程序
        System.exit(1);
    }

}
