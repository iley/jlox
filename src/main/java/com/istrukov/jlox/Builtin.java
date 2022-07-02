package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

public class Builtin {
    private Builtin() {
    }

    static final LoxCallable clock = new LoxCallable() {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, ImmutableList<Object> arguments) {
            return (double) System.currentTimeMillis() / 1000.0;
        }

        @Override
        public String toString() {
            return "<native clock>";
        }
    };
}
