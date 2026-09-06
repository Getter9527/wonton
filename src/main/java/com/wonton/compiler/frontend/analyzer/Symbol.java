package com.wonton.compiler.frontend.analyzer;

public class Symbol {

    private final String name;
    private final SemanticType type;

    public Symbol(String name, SemanticType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public SemanticType getType() {
        return type;
    }

}
