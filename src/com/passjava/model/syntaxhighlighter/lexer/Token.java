package com.passjava.model.syntaxhighlighter.lexer;

public final class Token {
    public final int start;
    public final int end;
    public final TokenType type;

    public Token(int start, int end, TokenType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }
}