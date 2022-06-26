package com.istrukov.jlox;

import javax.annotation.Nullable;
import javax.print.DocFlavor;

class Interpreter implements Visitor<Object> {
    void interpret(Expr expr) {
        try {
            var value = eval(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Nullable
    Object eval(Expr expression) {
        return expression.accept(this);
    }

    @Nullable
    @Override
    public Object visitBinary(Expr.Binary binary) {
        var left = eval(binary.left);
        var right = eval(binary.right);
        switch (binary.operator.type()) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } else if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(binary.operator, "operands must be either two numbers or two strings");
            case MINUS:
                checkNumberOperands(binary.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(binary.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(binary.operator, left, right);
                return (double)left * (double)right;
            case GREATER:
                checkBooleanOperands(binary.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkBooleanOperands(binary.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkBooleanOperands(binary.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkBooleanOperands(binary.operator, left, right);
                return (double)left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;
    }

    @Nullable
    @Override
    public Object visitUnary(Expr.Unary unary) {
        var right = eval(unary.right);
        switch (unary.operator.type()) {
            case MINUS:
                checkNumberOperand(unary.operator,right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object visitGrouping(Expr.Grouping grouping) {
        return eval(grouping.expression);
    }

    @Nullable
    @Override
    public Object visitLiteral(Expr.Literal literal) {
        if (literal.value.isBoolean()) {
            return literal.value.asBoolean();
        } else if (literal.value.isNumber()) {
            return literal.value.asNumber();
        } else if (literal.value.isString()) {
            return literal.value.asString();
        } else {
            return null;
        }
    }

    private boolean isTruthy(@Nullable Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean)value;
        }
        return true;
    }

    private boolean isEqual(@Nullable Object left, @Nullable Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    private void checkNumberOperands(Token operator, @Nullable Object left, @Nullable Object right) {
        if (!(left instanceof Double)) {
            throw new RuntimeError(operator, "left operand must be a number");
        }
        if (!(right instanceof Double)) {
            throw new RuntimeError(operator, "right operand must be a number");
        }
    }

    private void checkNumberOperand(Token operator, @Nullable Object operand) {
        if (!(operand instanceof Double)) {
            throw new RuntimeError(operator, "operand must be a number");
        }
    }

    private void checkBooleanOperand(Token operator, @Nullable Object operand) {
        if (!(operand instanceof Boolean)) {
            throw new RuntimeError(operator, "operand must be a boolean");
        }
    }

    private void checkBooleanOperands(Token operator, @Nullable Object left, @Nullable Object right) {
        if (!(left instanceof Boolean)) {
            throw new RuntimeError(operator, "left operand must be a boolean");
        }
        if (!(right instanceof Boolean)) {
            throw new RuntimeError(operator, "right operand must be a boolean");
        }
    }

    private static String stringify(@Nullable Object object) {
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length()-2);
            }
            return text;
        }
        return object.toString();
    }
}
