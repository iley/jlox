package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Nullable
    @Override
    public Object call(Interpreter interpreter, ImmutableList<Object> arguments) {
        var locals = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            locals.define(declaration.params.get(i).lexeme(), arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, locals);
        } catch (Return ret) {
            return ret.value;
        }
        return null;
    }
}
