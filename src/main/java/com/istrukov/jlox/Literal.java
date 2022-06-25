package com.istrukov.jlox;

import java.util.Optional;

public class Literal {
    private final Optional<String> stringValue;
    private final Optional<Double> numericValue;

    public Literal(String stringValue) {
        this.stringValue = Optional.ofNullable(stringValue);
        this.numericValue = Optional.empty();
    }

    public Literal(Double numericValue) {
        this.stringValue = Optional.empty();
        this.numericValue = Optional.ofNullable(numericValue);
    }

    public boolean isString() {
        return stringValue.isPresent();
    }

    public boolean isNumber() {
        return numericValue.isPresent();
    }

    public String asString() {
        return stringValue.get();
    }

    public Double asNumber() {
        return numericValue.get();
    }

    public String toString() {
        if (isString()) {
            return asString();
        } else {
            return asNumber().toString();
        }
    }
}
