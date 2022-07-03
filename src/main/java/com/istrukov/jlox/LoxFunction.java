package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Optional;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
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
            if (isInitializer) {
                return closure.getAt(0, new Token(TokenType.THIS, "this", Optional.empty(), 0));
            }
            return ret.value;
        }
        if (isInitializer) {
            return closure.getAt(0, new Token(TokenType.THIS, "this", Optional.empty(), 0));
        }
        return null;
    }

    public LoxFunction bind(LoxInstance loxInstance) {
        var environment = new Environment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(declaration, environment, isInitializer);
    }
}
