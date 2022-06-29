package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Optional;

abstract class Stmt extends AstNode {
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

    static class VariableDeclaration extends Stmt {
        final Token name;
        final Optional<Expr> initializer;

        VariableDeclaration(Token name, Optional<Expr> initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVar(this);
        }
    }

    static class Block extends Stmt {
        final ImmutableList<Stmt> statements;

        Block(ImmutableList<Stmt> statements) {
            this.statements = statements;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlock(this);
        }
    }

    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Optional<Stmt> elseBranch;

        If(Expr condition, Stmt thenBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = Optional.empty();
        }

        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = Optional.of(elseBranch);
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIf(this);
        }
    }
}
