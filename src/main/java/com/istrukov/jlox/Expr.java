package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

abstract class Expr extends AstNode {
    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }
    }

    static class Literal extends Expr {
        final Token.Literal value;

        Literal(Token.Literal value) {
            this.value = value;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    static class VariableReference extends Expr {
        final Token name;

        public VariableReference(Token name) {
            this.name = name;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableReference(this);
        }
    }

    static class Assignment extends Expr {
        final Token name;
        final Expr expression;

        Assignment(Token name, Expr expression) {
            this.name = name;
            this.expression = expression;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignment(this);
        }
    }

    static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogical(this);
        }
    }

    static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final ImmutableList<Expr> arguments;

        Call(Expr callee, Token paren, ImmutableList<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCall(this);
        }
    }

    static class Get extends Expr {
        final Expr object;
        final Token name;

        Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGet(this);
        }
    }

    static class Set extends Expr {
        final Expr object;
        final Token name;
        final Expr value;

        Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Nullable
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSet(this);
        }
    }
}
