package com.istrukov.jlox;

import javax.annotation.Nullable;

class AstPrinter implements Visitor<String> {
    @Nullable
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

    @Override
    public String visitVariableReference(Expr.VariableReference var) {
        return var.name.lexeme();
    }

    @Nullable
    @Override
    public String visitAssignment(Expr.Assignment assignment) {
        return String.format("(= %s %s)", assignment.name.lexeme(), assignment.expression.accept(this));
    }

    @Override
    public String visitExpression(Stmt.Expression expression) {
        return parenthesize("expr", expression.expression);
    }

    @Override
    public String visitPrint(Stmt.Print print) {
        return parenthesize("print", print.expression);
    }

    @Override
    public String visitVar(Stmt.VariableDeclaration variableDeclaration) {
        if (variableDeclaration.initializer.isPresent()) {
            return String.format("(var %s %s)", variableDeclaration.name.lexeme(), variableDeclaration.initializer.get().accept(this));
        } else {
            return String.format("(var %s)", variableDeclaration.name.lexeme());
        }
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
