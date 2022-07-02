package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

public interface LoxCallable {
    Object call(Interpreter interpreter, ImmutableList<Object> arguments);
    int arity();
}
