package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

public interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, ImmutableList<Object> arguments);
}
