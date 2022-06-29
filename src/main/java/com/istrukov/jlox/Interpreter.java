package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

class Interpreter implements Visitor<Object> {
    private Environment environment = new Environment();

    void interpret(ImmutableList<Stmt> program) {
        try {
            for (var stmt : program) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Nullable
    Object eval(Expr expression) {
        return expression.accept(this);
    }

    @Nullable
    @Override
    public Object visitVar(Stmt.VariableDeclaration variableDeclaration) {
        Object value = variableDeclaration.initializer.isPresent() ? eval(variableDeclaration.initializer.get()) : null;
        environment.define(variableDeclaration.name.lexeme(), value);
        return null;
    }

    @Nullable
    @Override
    public Object visitPrint(Stmt.Print print) {
        var result = eval(print.expression);
        System.out.println(stringify(result));
        return null;
    }

    @Nullable
    @Override
    public Object visitExpression(Stmt.Expression expression) {
        eval(expression.expression);
        return null;
    }

    @Nullable
    @Override
    public Object visitAssignment(Expr.Assignment assignment) {
        var value = eval(assignment.expression);
        environment.assign(assignment.name, value);
        return value;
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
                checkNumberOperands(binary.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(binary.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(binary.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(binary.operator, left, right);
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
    public Object visitLogical(Expr.Logical logical) {
        var leftResult = eval(logical.left);
        if (logical.operator.type() == TokenType.AND) {
            if (!isTruthy((leftResult))) {
                return false;
            }
        } else {
            if (isTruthy(leftResult)) {
                return true;
            }
        }
        return eval(logical.right);
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

    @Nullable
    @Override
    public Object visitVariableReference(Expr.VariableReference variableReference) {
        return environment.get(variableReference.name);
    }

    @Nullable
    @Override
    public Object visitBlock(Stmt.Block block) {
        executeBlock(block.statements, new Environment(environment));
        return null;
    }

    @Nullable
    @Override
    public Object visitIf(Stmt.If ifStmt) {
        var conditionResult = eval(ifStmt.condition);
        if (isTruthy(conditionResult)) {
            execute(ifStmt.thenBranch);
        } else if (ifStmt.elseBranch.isPresent()) {
            execute(ifStmt.elseBranch.get());
        }
        return null;
    }

    @Nullable
    @Override
    public Object visitWhile(Stmt.While whileLoop) {
        while(isTruthy(eval(whileLoop.condition))) {
            execute(whileLoop.body);
        }
        return null;
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

    void executeBlock(ImmutableList<Stmt> statements, Environment environment) {
        var previousEnvironment = this.environment;
        try {
            this.environment = environment;
            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previousEnvironment;
        }
    }
}
