package com.istrukov.jlox;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Environment {
    final Optional<Environment> enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = Optional.empty();
    }

    Environment(Environment enclosing) {
        this.enclosing = Optional.of(enclosing);
    }

    void define(String name, @Nullable Object value) {
        values.put(name, value);
    }

    public void assign(Token name, @Nullable Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }
        if (enclosing.isPresent()) {
            enclosing.get().assign(name, value);
            return;
        }
        throw new RuntimeError(name, String.format("undefined variable %s", name.lexeme()));
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }
        if (enclosing.isPresent()) {
            return enclosing.get().get(name);
        }
        throw new RuntimeError(name, String.format("undefined variable %s", name.lexeme()));
    }
}
