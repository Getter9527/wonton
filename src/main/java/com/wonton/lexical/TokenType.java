package com.wonton.lexical;

/**
 * 词素的标签类型
 */
public enum TokenType {
    Plus,               // +
    Minus,              // -
    Star,               // *
    Slash,              // /

    Equal,              // =
    Equalx2,            // ==
    Not,                // !
    NotEqual,           // !=
    Greater,            // >
    GreaterEqual,       // >=
    Less,               // <
    LessEqual,          // <=

    LeftParen,          // (
    RightParen,         // )
    LeftBracket,        // [
    RightBracket,       // ]
    LeftBrace,          // {
    RightBrace,         // }

    Dot,                // .
    Comma,              // ,
    Semicolon,          // ;

    And,
    Or,

    Identifier,
    If,
    Else,
    While,
    Return,

    Const,
    Variable,
    Integer,
    Decimal,
    String,
    Boolean,
    Null,
    Void,

    Print,
    Println,

    EOF,                // 文件结束
}
