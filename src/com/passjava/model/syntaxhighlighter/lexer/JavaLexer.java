package com.passjava.model.syntaxhighlighter.lexer;

import java.io.IOException;
import java.io.Reader;

public final class JavaLexer {
    private Reader reader;
    private int peek = -2;
    private int streamPos = 0;
    private static final String[] keywords = {"abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package",
            "synchronized", "do", "if", "private", "this", "break", "implements", "protected", "null", "true", "false",
            "throw", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
            "catch", "extends", "try", "final", "interface", "static", "void", "class",
            "finally", "strictfp", "volatile", "const", "native", "super", "while"};
    private static final String[] types = {"boolean", "double", "byte", "int", "short", "char", "long", "float"};

    public JavaLexer(Reader r) {
        this.reader = r;
    }

    private StringBuilder data = new StringBuilder();
    public Token nextToken() throws IOException {
        int c = read();
        if(c < 0)
            return null;
        State state = State.Identify;

        // Ignore whitespace
        while(Character.isWhitespace(c) && c >= 0)
            c = read();
        if(c < 0)
            return null;

        data.delete(0, data.length());
        int symbStart = streamPos - 1;
        int pos = 0;
        boolean foundDec = false, foundExp = false, foundEnd = false, hex = false; // number parsing variables
        boolean escaped = false; // string parsing
        boolean multiline = false, hadstar=false; // comment parsing
        while(true) {
            switch(state) {
                // find out what type we're dealing with...
                case Identify:
                    if(Character.isDigit(c))
                        state = State.Numerical;
                    else if(Character.isJavaIdentifierStart(c))
                        state = State.Word;
                    else if(c == '"')
                        state = State.DoubleQuotedString;
                    else if(c == '\'')
                        state = State.SingleQuotedString;
                    else if(c == '/')
                        state = State.Comment;
                    else if(c == '!' || c == '%' || c == '^' || c == '&' || c == '*' || c == '(' || c == ')' || c == '-'
                            || c == '+' || c == '=' || c == '[' || c == '{' || c == ']' || c == '}' || c == ';'
                            || c == ':' || c == '~' || c == '|' || c == '<' || c == ',' || c == '>' || c == '.'
                            || c == '?')
                    return new Token(symbStart, symbStart+1, TokenType.Operator);
                    else
                        state = State.Unknown;
                    break;

                case Numerical:
                    if(pos == 1 && c == 'x') // hex!
                         hex = true;
                    else if(!hex && !foundDec && !foundExp && c == '.')
                        foundDec = true;
                    else if(!hex && !foundExp && c == 'e')
                        foundExp = true;
                    else if(!foundEnd && (c == 'd' || c == 'D' || c == 'f' || c == 'F' || c == 'l' || c == 'L'))
                        foundEnd = true;
                    else if (!Character.isDigit(c) &&
                            (!hex || ((c < 'a' || c > 'f') && (c < 'A' || c > 'F')))) {
                        peek = c;
                        return new Token(symbStart, streamPos - 1, TokenType.Numerical);
                    }
                    break;

                case SingleQuotedString: // character declaration...
                    if(!escaped && c == '\\')
                        escaped = true;
                    else if(!escaped && c == '\'')
                        return new Token(symbStart, streamPos - 1, TokenType.String);
                    else if(c == '\r' || c == '\n' || c < 0) {
                        peek = c;
                        return new Token(symbStart, streamPos - 1, TokenType.String);
                    }
                    else
                        escaped = false;
                    break;

                case DoubleQuotedString:
                    if(!escaped && c == '\\')
                        escaped = true;
                    else if(!escaped && c == '\"')
                        return new Token(symbStart, streamPos, TokenType.String);
                    else if(c == '\r' || c == '\n' || c < 0) {
                        peek = c;
                        return new Token(symbStart, streamPos - 1, TokenType.String);
                    }
                    else
                        escaped = false;
                    break;

                case Word:
                    if(!Character.isJavaIdentifierPart(c)) {
                        peek = c;
                        String s = data.toString();
                        if(isKeyword(s))
                            return new Token(symbStart, streamPos - 1, TokenType.Keyword);
                        if(isType(s))
                            return new Token(symbStart, streamPos - 1, TokenType.Type);
                        return new Token(symbStart, streamPos - 1, TokenType.Identifier);
                    }
                    break;

                case Comment:
                    if(pos == 1 && c == '*')
                        multiline = true;
                    else if(multiline && c == '*')
                        hadstar = true;
                    else if(hadstar && c == '/') {
                        return new Token(symbStart, streamPos, TokenType.Comment);
                    } else
                        hadstar = false;

                    if((!multiline && (c == '\n' || c == '\r')) || c < 0) {
                        peek = c;
                        return new Token(symbStart, streamPos - 1, TokenType.Comment);
                    }
                    break;

                case Unknown:
                    if(Character.isWhitespace(c) || c < 0) {
                        peek = c;
                        return new Token(symbStart, streamPos - 1, TokenType.Identifier);
                    }
                    break;

                default:
                    System.err.println("BROKED on " + (char) c);
            }
            if(c < 0)
                return null;
            data.append((char) c);
            c = read();
            if(c < 0)
                streamPos += 1; // hack
            pos++;
        }
    }

    private int read() throws IOException {
        if(peek != -2) {
            int val = peek;
            peek = -2;
            return val;
        }

        int i = reader.read();
        if(i != -1)
            streamPos++;
        return i;
    }

    private static boolean isKeyword(String s) {
        for(String key: keywords)
            if(key.equals(s))
                return true;
        return false;
    }

    public static boolean isType(String s) {
        for(String t: types)
            if(t.equals(s))
                return true;
        return false;
    }

    private enum State {
        Identify,
        Numerical,
        Word,
        SingleQuotedString,
        DoubleQuotedString,
        Comment,
        Unknown
    }
}