package com.istrukov.jlox;

interface Visitor<R> {
    R visitBinary(Expr.Binary binary);
    R visitUnary(Expr.Unary unary);
    R visitGrouping(Expr.Grouping grouping);
    R visitLiteral(Expr.Literal literal);
}