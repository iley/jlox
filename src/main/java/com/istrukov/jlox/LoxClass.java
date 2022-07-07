package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Optional;

public class LoxClass implements LoxCallable {
    final String name;
    final Optional<LoxClass> superclass;
    private final ImmutableMap<String, LoxFunction> methods;

    public LoxClass(String name, Optional<LoxClass> superclass, ImmutableMap<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        var initializer = methods.get("init");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }

    @Nullable
    @Override
    public Object call(Interpreter interpreter, ImmutableList<Object> arguments) {
        var instance = new LoxInstance(this);
        var initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, ImmutableList.of());
        }
        return instance;
    }

    Optional<LoxFunction> findMethod(String name) {
        if (methods.containsKey(name)) {
            return Optional.of(methods.get(name));
        }
        return superclass.flatMap(superclass -> superclass.findMethod(name));
    }
}
