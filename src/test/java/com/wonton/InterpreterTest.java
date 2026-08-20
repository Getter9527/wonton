package com.wonton;

import com.wonton.interpreter.TreeWalkingInterpreter;
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
        TreeWalkingInterpreter interpreter = new TreeWalkingInterpreter();
        Object result = interpreter.interpret(ast);
        Logger.success("结果: {0}", result);
    }
}
