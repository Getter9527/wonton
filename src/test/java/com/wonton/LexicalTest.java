package com.wonton;

import com.wonton.lexical.Lexer;
import com.wonton.lexical.Token;
import com.wonton.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

public class LexicalTest {
    @Test
    void testReadFile() {
        Stream<String> fileStream = FileUtils.open("scripts/hello.wonton");
        fileStream.forEach(System.out::println);
    }

    @Test
    void testTokenize() {
        final String source = FileUtils.readSource("scripts/hello.wonton");
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("[token] ----------------------------------------------------------");
        for (var i = 0; i < tokens.size(); i++) {
            System.out.printf("序号: %-6s %s%n", i+1, tokens.get(i));
        }
        System.out.println("------------------------------------------------------------------");
    }
}
