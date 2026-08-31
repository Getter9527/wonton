package com.wonton;

import com.wonton.interpreter.Interpreter;
import com.wonton.interpreter.RuntimeValue;
import com.wonton.lexical.Lexer;
import com.wonton.lexical.Token;
import com.wonton.logger.Logger;
import com.wonton.syntax.Parser;
import com.wonton.syntax.node.Node;
import com.wonton.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

public class InterpreterTest {

    @Test
    public void test() {
        // 源代码
        String source = FileUtils.readSource("scripts/simple_expression.wonton");

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testStringExpression() {
        // 源代码
        String source = FileUtils.readSource("scripts/string_expression.wonton");
        Logger.debug("源代码: {0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testComparisonExpression() {
        // 源代码
        String source = FileUtils.readSource("scripts/comparison_expression.wonton");
        Logger.debug("源代码: {0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testEqualityExpression() {
        // 源代码
        String source = FileUtils.readSource("scripts/equality_expression.wonton");
        Logger.debug("源代码: {0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testLogicalAndExpression() {
        // 源代码
        String source = FileUtils.readSource("scripts/logical_expression.wonton");
        Logger.debug("源代码: {0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testModuloExpression() {
        // 源代码
        String source = FileUtils.readSource("scripts/modulo_expression.wonton");
        Logger.debug("源代码:\n{0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testExponentExpression() {
        // 源代码
        String source = FileUtils.readSource("scripts/exponent_expression.wonton");
        Logger.debug("源代码:\n{0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }

    @Test
    public void testPrintStatement() {
        // 源代码
        String source = FileUtils.readSource("scripts/print_statement.wonton");
        Logger.debug("源代码:\n{0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(ast);
    }

    @Test
    public void testIfStatement() {
        // 源代码
        String source = FileUtils.readSource("scripts/if_statement.wonton");
        Logger.debug("源代码:\n{0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(ast);
    }

    @Test
    public void testVariableStatement() {
        // 源代码
        String source = FileUtils.readSource("scripts/variable_statement.wonton");
        Logger.debug("源代码:\n{0}", source);

        // tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (var i = 0; i < tokens.size(); i++) {
            Logger.info("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.info("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(ast);
    }

}
