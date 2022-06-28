package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import java.util.Optional;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final ImmutableList<Token> tokens;
    private int current = 0;

   Parser(ImmutableList<Token> tokens) {
        this.tokens = tokens;
    }

    ImmutableList<Stmt> parse() {
       try {
           return program();
       } catch (ParseError error) {
           return ImmutableList.of();
       }
    }

    private ImmutableList<Stmt> program() {
       var builder = ImmutableList.<Stmt>builder();
       while (!isAtEnd()) {
           var decl = declaration();
           if (decl.isPresent()) {
               builder.add(decl.get());
           }
       }
       return builder.build();
    }

    private Optional<Stmt> declaration() {
       try {
           if (match(TokenType.VAR)) {
               return Optional.of(varDeclaration());
           }
           return Optional.of(statement());
       } catch (ParseError error) {
           synchronize();
           return Optional.empty();
       }
    }

    private Stmt varDeclaration() {
       var name = consume(TokenType.IDENTIFIER, "expected an identifier in a variable declaration");
       Optional<Expr> initializer = match(TokenType.EQUAL) ? Optional.of(expression()) : Optional.empty();
       consume(TokenType.SEMICOLON, "expected semicolon after variable declaration");
       return new Stmt.VariableDeclaration(name, initializer);
    }

    private Stmt statement() {
       if (match(TokenType.PRINT)) {
           return printStatement();
       }
       if (match(TokenType.LEFT_BRACE)) {
           return new Stmt.Block(block());
       }
       var expr = expression();
       consume(TokenType.SEMICOLON, "expected ; after expression statement");
       return new Stmt.Expression(expr);
    }

    private Stmt printStatement() {
       var expr = expression();
        consume(TokenType.SEMICOLON, "expected ; after expression in print");
       return new Stmt.Print(expr);
    }

    private ImmutableList<Stmt> block() {
       var builder = ImmutableList.<Stmt>builder();
       while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
           var decl = declaration();
           if (decl.isPresent()) {
               builder.add(decl.get());
           }
       }
       consume(TokenType.RIGHT_BRACE, "expected } at the end of a block");
       return builder.build();
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
       var expr = equality();
       if (match(TokenType.EQUAL)) {
           var equals = previous();
           var value = assignment();
           if (expr instanceof Expr.VariableReference) {
               var name = ((Expr.VariableReference)expr).name;
               return new Expr.Assignment(name, value);
           }
           error(equals, "invalid assignment target");
       }
       return expr;
    }

    private Expr equality() {
        var expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            var operator = previous();
            var right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        var expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            var operator = previous();
            var right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
         var expr = factor();
         while (match(TokenType.MINUS, TokenType.PLUS)) {
             var operator = previous();
             var right = factor();
             expr = new Expr.Binary(expr, operator, right);
         }
         return expr;
    }

    private Expr factor() {
        var expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            var operator = previous();
            var right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            var operator = previous();
            var right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE, TokenType.TRUE, TokenType.NIL, TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal().get());
        }
        if (match(TokenType.LEFT_PAREN)) {
            var expr = expression();
            consume(TokenType.RIGHT_PAREN, "expected ')' after expression");
            return new Expr.Grouping(expr);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.VariableReference(previous());
        }
        throw error(peek(), "expected an expression");
    }

    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type() == TokenType.SEMICOLON) {
                return;
            }
            switch (peek().type()) {
                case CLASS: case FOR: case FUN: case IF: case PRINT: case RETURN: case VAR: case WHILE:
                    return;
            }
            advance();
        }
    }
}
