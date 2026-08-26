package com.wonton.syntax;

import com.wonton.lexical.Token;
import com.wonton.lexical.TokenType;
import com.wonton.logger.Logger;
import com.wonton.syntax.node.Node;
import com.wonton.syntax.node.expression.*;
import com.wonton.syntax.node.statement.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
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

    public Node parse() {
        Stmts ast = program();
        return ast;
    }

    private Stmts program() {
        Stmts stmts = stmts();
        return stmts;
    }

    private Stmts stmts() {
        List<Stmt> stmtList = new ArrayList<>();
        // 只要有未消费的tokens，那么就继续消费
        while (current < tokens.size()) {
            // 按照句子去消费 tokens
            Stmt stmt = stmt();
            stmtList.add(stmt);
        }
        return new Stmts(stmtList);
    }

    private Stmt stmt() {
        // TODO 解析 print语句、if语句、while语句、for语句、assignment语句、function call、etc
        Token token = peek();
        switch (token.getType()) {
            case Print -> {
                return printStmt();
            }
            case If -> {
                return ifStmt();
            }
            case While -> {
                return whileStmt();
            }
            case For -> {
                return forStmt();
            }
            case Function -> {
                return functionDeclaration();
            }
        }
        parseError(MessageFormat.format("意外的符号，无法解析为语句：{0}", token.getLexeme()), token.getLine());
        return null;
    }

    private Stmt printStmt() {
        consume(TokenType.Print);
        Expr value = expr();
        consume(TokenType.Semicolon);
        return new PrintStmt(value);
    }

    private Stmt ifStmt() {
        consume(TokenType.If);
        Expr condition = expr();
        if (condition == null) {
            parseError("if 后面缺少条件表达式", previous().getLine());
        }
        BlockStmt ifBlock = blockStmt();
        // 仅if语句
        if (!match(TokenType.Else)) {
            return new IfStmt(condition, ifBlock, null);
        }
        // if...else语句
        BlockStmt elseBlock = blockStmt();
        return new IfStmt(condition, ifBlock, elseBlock);
    }

    private Stmt whileStmt() {
        return null;
    }

    private Stmt forStmt() {
        return null;
    }

    private Stmt functionDeclaration() {
        return null;
    }

    private BlockStmt blockStmt() {
        if (!match(TokenType.LeftBrace)) {
            throw new RuntimeException("缺少左大括号：{");
        }
        List<Stmt> stmtList = new ArrayList<>();
        // 遇到右大括号或 token 耗尽时停止，右大括号留给本方法末尾统一消费和校验
        while (current < tokens.size()) {
            if (peek().getType() == TokenType.RightBrace) {
                break;
            }
            stmtList.add(stmt());
        }
        if (!match(TokenType.RightBrace)) {
            throw new RuntimeException("语句块未闭合，缺少右大括号：}");
        }
        return new BlockStmt(stmtList);
    }

    private Expr expr() {
        return logicalOr();
    }

    /**
     * 逻辑或
     * @return expr
     */
    private Expr logicalOr() {
        // <expr> ::= <or> ("or" <or>)*
        Expr expr = logicalAnd();
        while (matchAny(TokenType.Or)) {
            Token operator = previous();
            Expr right = logicalAnd();
            // 左结合律：expr = (left or right) or right
            expr = new LogicalExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 逻辑与
     * @return or
     */
    private Expr logicalAnd() {
        // <or> ::= <and> ("and" <and>)*
        Expr expr = equality();
        while (matchAny(TokenType.And)) {
            Token operator = previous();
            Expr right = equality();
            expr = new LogicalExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 相等、不相等
     * @return and
     */
    private Expr equality() {
        // <and> ::= <equality> (<op> <equality>)*
        // <op> ::= "=" | "!="
        // 先解析第1个比较数
        Expr expr = comparison();
        while (matchAny(TokenType.Equalx2, TokenType.NotEqual)) {
            // 获取操作符（=、!=）
            Token operator = previous();
            // 解析第2个比较数
            Expr right = comparison();
            // 构建关系运算
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 关系运算，比较层
     * @return expr
     */
    private Expr comparison() {
        // <equality> ::= <comparison> (<op> <comparison>)*
        // <op> ::= "<" | ">" | "<=" | ">="
        // 先解析第1个比较数
        Expr expr = addition();
        // 匹配比较运算符
        while (matchAny(TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual)) {
            // 获取操作符（<、<=、>、>=）
            Token operator = previous();
            // 解析第2个比较数
            Expr right = addition();
            // 构建关系运算
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 加减法
     * @return comparison(term | factor | unary | primary)
     */
    private Expr addition() {
        // <comparison> ::= <term> (<op> <term>)*
        // <op>   ::= "+" | "-"
        // 先解析首个项（也是左操作数）
        Expr expr = multiplication();
        // 实现左结合的加减运算
        while (matchAny(TokenType.Plus, TokenType.Minus)) {
            // 获取操作符（+、-）
            Token operator = previous();
            // 获取右操作数
            Expr right = multiplication();
            // 构建二元表达式
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 乘除法
     * @return term(factor | unary | primary)
     */
    private Expr multiplication() {
        // <term> ::= <factor> (<op> <factor>)*
        // <op>   ::= "×" | "÷"
        Expr expr = modulo();
        while (matchAny(TokenType.Star, TokenType.Slash)) {
            // 获取操作符（*、/）
            Token operator = previous();
            // 获取右操作数
            Expr right = modulo();
            // 构建二元表达式
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 取模运算
     * @return factor
     */
    private Expr modulo() {
        Expr expr = unary();
        while (matchAny(TokenType.Modulo)) {
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 一元运算
     * @return unary | primary
     */
    private Expr unary() {
        // <unary>  ::= <op> <unary> | <exponent>
        // <op>     ::= "+" | "-" | "!"
        if (matchAny(TokenType.Plus, TokenType.Minus, TokenType.Not)) {
            // 获取操作符（+、-、!）
            Token operator = previous();
            // TODO 如果是不合法的连续一元操作，这里还真不好处理，直接逻辑判断是不好使的
            // 右侧的操作数本身可能也是一个一元表达式，所以这里采用右递归文法（从右向左结合）
            Expr operand = unary();
            // 右结合律：unary = -(-operand)
            return new UnaryExpr(operator, operand);
        }
        // 当没有匹配到一元运算符时，不再递归
        return exponent();
    }

    /**
     * 指数运算
     * @return exponent
     */
    private Expr exponent() {
        // TODO 目前不支持 2 ^ -3 ^ 4，中间出现一元运算的这种情况
        Expr expr = primary();
        while (matchAny(TokenType.Caret)) {
            Token operator = previous();
            Expr right = exponent(); // 右结合
            expr = new BinaryExpr(operator, expr, right);
        }
        return expr;
    }

    /**
     * 基础数据类型、括号
     * <p>primary 是表达式的最小原子单元，不可再被运算符拆分的基本元素，或者是一个被括号包裹的完整子表达式。           </p>
     * <p>括号是 primary 的特殊形式，它的语义是"把子表达式打包成一个整体"，通过递归调用 expr() 解析这个内部的子表达式。</p>
     *
     * @return primary
     */
    private Expr primary() {
        // <primary> ::= <integer> | <decimal> | <boolean> | <string> | <paren>
        // <boolean>::= "true" | "false"
        // <paren>  ::= "(" <expr> ")"
        if (match(TokenType.Integer)) {
            Long literal = (Long) previous().getLiteral();
            return new IntegerExpr(literal);
        }
        if (match(TokenType.Decimal)) {
            BigDecimal literal = (BigDecimal) previous().getLiteral();
            return new DecimalExpr(literal);
        }
        if (match(TokenType.String)) {
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
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * 获取上一个 Token信息
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * 推进 Token
     * @description 获取当前正在处理的 Token，并向后推进
     */
    private Token advance() {
        Token token = tokens.get(current);
        current++;
        return token;
    }

    /**
     * 仅消费Token，什么也不做
     */
    private void consume(TokenType type) {
        if (match(type)) {
            return;
        }
        parseError("期望TokenType:" + type, previous().getLine());
    }

    /**
     * 匹配并消费 Token
     * @description 匹配当前 Token 是否为指定类型，如果是则消费之；否则不消费
     */
    private boolean match(TokenType type) {
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
    private boolean matchAny(TokenType ...types) {
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

    private Token expect(TokenType type) {
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
