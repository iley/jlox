package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

public class LoxClass implements LoxCallable {
    final String name;

    public LoxClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Nullable
    @Override
    public Object call(Interpreter interpreter, ImmutableList<Object> arguments) {
        var instance = new LoxInstance(this);
        return instance;
    }
}
