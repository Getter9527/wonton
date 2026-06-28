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
        String source = FileUtils.readSource("scripts/hello.wonton");
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("------------------------------------------------------------------");
        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println("------------------------------------------------------------------");
    }
}
