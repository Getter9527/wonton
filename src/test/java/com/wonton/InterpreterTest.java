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
            Logger.debug("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.debug("\n" + ast.toPrettyString());

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
            Logger.debug("序号: {0} {1}", String.format("%-6s", i+1), tokens.get(i));
        }

        // 语法树
        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        Logger.debug("\n" + ast.toPrettyString());

        // 解释执行
        Interpreter interpreter = new Interpreter();
        RuntimeValue result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }
}
