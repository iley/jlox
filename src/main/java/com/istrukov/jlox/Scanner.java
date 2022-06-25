package com.istrukov.jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scanner {
    private final String input;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String input) {
        this.input = input;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", Optional.empty(), line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            default:
                Lox.error(line, String.format("Unexpected character %s", c));
        }
    }

    private char advance() {
        return input.charAt(current++);
    }

    private void addToken(TokenType type) {
        String text = input.substring(start, current);
        tokens.add(new Token(type, text, Optional.empty(), line));
    }

    private void addToken(TokenType type, Literal literal) {
        String text = input.substring(start, current);
        tokens.add(new Token(type, text, Optional.of(literal), line));
    }

    private boolean isAtEnd() {
        return current >= input.length();
    }
}
