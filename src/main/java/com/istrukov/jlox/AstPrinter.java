package com.istrukov.jlox;

class AstPrinter implements Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
   public String visitBinary(Expr.Binary binary) {
        return parenthesize(binary.operator.lexeme(), binary.left, binary.right);
    }

    @Override
    public String visitUnary(Expr.Unary unary) {
        return parenthesize(unary.operator.lexeme(), unary.right);
    }

    @Override
    public String visitGrouping(Expr.Grouping grouping) {
        return parenthesize("group", grouping.expression);
    }

    @Override
    public String visitLiteral(Expr.Literal literal) {
        return literal.value.toString();
    }

    private String parenthesize(String name, Expr... exprs) {
        var builder = new StringBuilder();
        builder.append("(").append(name);
        for (var expr : exprs) {
            builder.append(" ").append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}
