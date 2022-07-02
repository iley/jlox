package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

public interface LoxCallable {
    int arity();

    @Nullable
    Object call(Interpreter interpreter, ImmutableList<Object> arguments);
}
