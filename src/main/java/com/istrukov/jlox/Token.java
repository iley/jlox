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

    static class Literal {
        private final Optional<String> stringValue;
        private final Optional<Double> numericValue;
        private final Optional<Boolean> booleanValue;
        private final boolean isNil;

        Literal(String stringValue) {
            this.stringValue = Optional.ofNullable(stringValue);
            this.numericValue = Optional.empty();
            this.booleanValue = Optional.empty();
            this.isNil = false;
        }

        Literal(Double numericValue) {
            this.stringValue = Optional.empty();
            this.numericValue = Optional.ofNullable(numericValue);
            this.booleanValue = Optional.empty();
            this.isNil = false;
        }

        Literal(Boolean booleanValue) {
            this.stringValue = Optional.empty();
            this.numericValue = Optional.empty();
            this.booleanValue = Optional.of(booleanValue);
            this.isNil = false;
        }

        Literal() {
            this.stringValue = Optional.empty();
            this.numericValue = Optional.empty();
            this.booleanValue = Optional.empty();
            this.isNil = true;
        }

        boolean isString() {
            return stringValue.isPresent();
        }

        boolean isNumber() {
            return numericValue.isPresent();
        }

        boolean isBoolean() {
            return booleanValue.isPresent();
        }

        boolean isNil() {
            return isNil;
        }

        String asString() {
            return stringValue.get();
        }

        Double asNumber() {
            return numericValue.get();
        }

        Boolean asBoolean() {
            return booleanValue.get();
        }

        public String toString() {
            if (isString()) {
                return asString();
            } else if (isNumber()) {
                return asNumber().toString();
            } else if (isBoolean()) {
                return asBoolean().toString();
            } else {
                return "nil";
            }
        }
    }
}
