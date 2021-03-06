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
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpression(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrint(this);
        }
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

    static class While extends Stmt {
        final Expr condition;
        final Stmt body;

        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhile(this);
        }
    }

    static class Function extends Stmt {
        final Token name;
        final ImmutableList<Token> params;
        final ImmutableList<Stmt> body;

        Function(Token name, ImmutableList<Token> params, ImmutableList<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunction(this);
        }
    }

    static class Return extends Stmt {
        final Token keyword;
        final Optional<Expr> value;

        Return(Token keyword, Optional<Expr> value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturn(this);
        }
    }

    @SuppressWarnings("JavaLangClash")
    static class Class extends Stmt {
        final Token name;
        final Optional<Expr.VariableReference> superclass;
        final ImmutableList<Stmt.Function> methods;

        Class(Token name, Optional<Expr.VariableReference> superclass, ImmutableList<Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClass(this);
        }
    }
}
