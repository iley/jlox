package com.istrukov.jlox;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }
        throw new RuntimeError(name, String.format("Undefined property '%s'", name.lexeme()));
    }

    public void set(Token name, @Nullable Object value) {
        fields.put(name.lexeme(), value);
    }
}
