package com.istrukov.jlox;

import javax.annotation.Nullable;

interface Visitor<R> {
    @Nullable
    R visitBinary(Expr.Binary binary);
    @Nullable
    R visitUnary(Expr.Unary unary);
    @Nullable
    R visitGrouping(Expr.Grouping grouping);
    @Nullable
    R visitLiteral(Expr.Literal literal);

    @Nullable
    R visitExpression(Stmt.Expression expression);

    @Nullable
    R visitPrint(Stmt.Print print);

    @Nullable
    R visitVar(Stmt.Var var);

    @Nullable
    R visitVariable(Expr.Variable variable);
}