package com.wonton.lexical;

/**
 * 词素的标签类型
 */
public enum TokenType {
    Plus,               // +
    Minus,              // -
    Star,               // *
    Slash,              // /
    Dot,                // .
    Comma,              // ,
    Semicolon,          // ;
    Equal,              // =
    EqualX2,            // ==
    NOT,                // !
    NotEqual,           // !=
    GT,                 // >
    GE,                 // >=
    LT,                 // <
    LE,                 // <=
    LeftParen,          // (
    RightParen,         // )
    LeftBracket,        // [
    RightBracket,       // ]
    LeftBrace,          // {
    RightBrace,         // }

    Identifier,
    If,
    Else,
    While,
    Return,

    Const,
    Var,
    Int,
    Decimal,
    Str,
    Void,

    Print,
    Println,
    True,
    False,
    Null,
}
