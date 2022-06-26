package com.istrukov.jlox;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void define(String name, @Nullable Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }
        throw new RuntimeError(name, String.format("undefined variable %s", name.lexeme()));
    }
}
