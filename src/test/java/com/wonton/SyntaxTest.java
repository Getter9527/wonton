package com.wonton;

import com.wonton.lexical.Lexer;
import com.wonton.lexical.Token;
import com.wonton.syntax.Parser;
import com.wonton.syntax.node.Node;
import com.wonton.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SyntaxTest {

    @Test
    void testParse() {
        final String source = FileUtils.readSource("scripts/simple_expression.wonton");
        System.out.println("[source] --------------------------------------------------------------");
        System.out.println(source);
        System.out.println("-----------------------------------------------------------------------");

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("[token] ---------------------------------------------------------------");
        for (var i = 0; i < tokens.size(); i++) {
            System.out.printf("序号: %-6s %s%n", i+1, tokens.get(i));
        }
        System.out.println("-----------------------------------------------------------------------");

        Parser parser = new Parser(tokens);
        Node ast = parser.parse();
        System.out.println("[ast] -----------------------------------------------------------------");
        System.out.println(ast.toPrettyString());
        System.out.println("-----------------------------------------------------------------------");
    }
}
