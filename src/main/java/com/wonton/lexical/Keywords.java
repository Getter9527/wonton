package com.wonton.lexical;

import java.util.HashMap;
import java.util.Map;

/**
 * 关键字工具类
 */
public class Keywords {

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("if", TokenType.If);
        keywords.put("else", TokenType.Else);
        keywords.put("while", TokenType.While);
        keywords.put("return", TokenType.Return);
        keywords.put("const", TokenType.Const);
        keywords.put("true", TokenType.Boolean);
        keywords.put("false", TokenType.Boolean);
        keywords.put("var", TokenType.Var);
        keywords.put("int", TokenType.Int);
        keywords.put("decimal", TokenType.Decimal);
        keywords.put("str", TokenType.Str);
        keywords.put("void", TokenType.Void);
        keywords.put("print", TokenType.Print);
        keywords.put("println", TokenType.Println);
        keywords.put("null", TokenType.Null);
    }

    public static TokenType getType(String lexeme) {
        return keywords.get(lexeme);
    }

    public static boolean isKeyword(String lexeme) {
        return keywords.containsKey(lexeme);
    }

}
