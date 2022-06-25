package com.istrukov.jlox;

import java.util.Optional;

public record Token(
        TokenType type,
        String lexeme,
        Optional<Literal> literal,
        int line
) {
    public String toString() {
        if (literal.isPresent()) {
            return String.format("%s %s %s", type, lexeme, literal.get());
        } else {
            return String.format("%s %s", type, lexeme);
        }
    }
}
