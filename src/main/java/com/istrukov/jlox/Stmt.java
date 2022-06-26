package com.istrukov.jlox;

import javax.annotation.Nullable;
import java.util.Optional;

abstract class Stmt {
    @Nullable
    abstract <R> R accept(Visitor<R> visitor);

    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) { return visitor.visitExpression(this); }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) { return visitor.visitPrint(this); }
    }

    static class Var extends Stmt {
        final Token name;
        final Optional<Expr> initializer;

        Var(Token name, Optional<Expr> initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVar(this);
        }
    }
}
