package com.istrukov.jlox;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scanner {
    private final String input;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final ImmutableMap<String, TokenType> keywords = ImmutableMap.<String, TokenType>builder()
            .put("and", TokenType.AND)
            .put("class", TokenType.CLASS)
            .put("else", TokenType.ELSE)
            .put("false", TokenType.FALSE)
            .put("for", TokenType.FOR)
            .put("fun", TokenType.FUN)
            .put("if", TokenType.IF)
            .put("nil", TokenType.NIL)
            .put("or", TokenType.OR)
            .put("print", TokenType.PRINT)
            .put("return", TokenType.RETURN)
            .put("super", TokenType.SUPER)
            .put("this", TokenType.THIS)
            .put("true", TokenType.TRUE)
            .put("var", TokenType.VAR)
            .put("while", TokenType.WHILE)
            .build();

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
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL: TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    scanComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line++;
                break;
            case '"':
                scanString();
                break;
            default:
                if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifier();
                } else {
                    Lox.error(line, String.format("Unexpected character %s", c));
                }
        }
    }

    private void scanIdentifier() {
        while (isAlphanumeric(peek())) {
            advance();
        }
        var text = input.substring(start, current);
        var type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private  void scanString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "unterminated string");
            return;
        }
        advance(); // terminating '"'
        var value = input.substring(start+1, current-1);
        addToken(TokenType.STRING, new Literal(value));
    }

    private void scanNumber() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // '.'
            while (isDigit(peek())) {
                advance();
            }
        }

        var value = Double.parseDouble(input.substring(start, current));
        addToken(TokenType.NUMBER, new Literal(value));
    }

    private void scanComment() {
        while (peek() != '*' || peekNext() != '/') {
            if (peek() == '\n') line++;
            advance();
        }
        advance(); // '*'
        advance(); // '/'
    }

    private char advance() {
        if (isAtEnd()) {
            return '\0';
        }
        return input.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (input.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return input.charAt(current);
    }

    private char peekNext() {
        if (current+1 >= input.length()) {
            return '\0';
        }
        return input.charAt(current+1);
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

    private static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_';
    }

    private static boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
