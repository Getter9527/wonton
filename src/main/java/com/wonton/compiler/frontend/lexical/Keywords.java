package com.wonton.compiler.frontend.lexical;

import java.util.HashMap;
import java.util.Map;

/**
 * 关键字工具类
 */
public class Keywords {

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("if",       TokenType.If);
        keywords.put("else",     TokenType.Else);
        keywords.put("while",    TokenType.While);
        keywords.put("for",      TokenType.For);
        keywords.put("print",    TokenType.Print);
        keywords.put("println",  TokenType.Println);
        keywords.put("and",      TokenType.And);
        keywords.put("or",       TokenType.Or);
        keywords.put("null",     TokenType.Null);
        keywords.put("true",     TokenType.Boolean);
        keywords.put("false",    TokenType.Boolean);
        keywords.put("var",      TokenType.Variable);
        keywords.put("const",    TokenType.Const);
        keywords.put("void",     TokenType.Void);
        keywords.put("return",   TokenType.Return);
        keywords.put("function", TokenType.Function);
        keywords.put("int",      TokenType.TypeInteger);
        keywords.put("decimal",  TokenType.TypeDecimal);
        keywords.put("string",   TokenType.TypeString);
        keywords.put("boolean",  TokenType.TypeBoolean);
    }

    public static TokenType getType(String lexeme) {
        return keywords.get(lexeme);
    }

    public static boolean isKeyword(String lexeme) {
        return keywords.containsKey(lexeme);
    }

}
