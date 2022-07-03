package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

class Interpreter implements Visitor<Object> {
    Environment globals = new Environment();
    private Environment environment = globals;
    Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", Builtin.clock);
    }

    private static String stringify(@Nullable Object object) {
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

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
        Object value = variableDeclaration.initializer.map(this::eval).orElse(null);
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
        var distance = locals.get(assignment);
        if (distance != null) {
            environment.assignAt(distance, assignment.name, value);
        } else {
            globals.assign(assignment.name, value);
        }
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
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(binary.operator, "operands must be either two numbers or two strings");
            case MINUS:
                checkNumberOperands(binary.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(binary.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(binary.operator, left, right);
                return (double) left * (double) right;
            case GREATER:
                checkNumberOperands(binary.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(binary.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(binary.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(binary.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object visitLogical(Expr.Logical logical) {
        var leftResult = eval(logical.left);
        if (logical.operator.type() == TokenType.AND) {
            if (!isTruthy(leftResult)) {
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
                checkNumberOperand(unary.operator, right);
                return -(double) right;
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
        return lookupVariable(variableReference.name, variableReference);
    }

    private Object lookupVariable(Token name, Expr.VariableReference variableReference) {
        var distance = locals.get(variableReference);
        if (distance != null) {
            return environment.getAt(distance, name);
        }
        return globals.get(name);
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
        while (isTruthy(eval(whileLoop.condition))) {
            execute(whileLoop.body);
        }
        return null;
    }

    @Nullable
    @Override
    public Object visitCall(Expr.Call call) {
        Object callee = eval(call.callee);
        if (!(callee instanceof LoxCallable callable)) {
            throw new RuntimeError(call.paren, "can only call functions and classes");
        }
        var args = call.arguments.stream().map(this::eval).collect(ImmutableList.toImmutableList());
        if (args.size() != callable.arity()) {
            throw new RuntimeError(call.paren, String.format("wrong number of arguments in a function call, expected %d, got %d", callable.arity(), args.size()));
        }
        return callable.call(this, args);
    }

    private boolean isTruthy(@Nullable Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return true;
    }

    @Nullable
    @Override
    public Object visitFunction(Stmt.Function fun) {
        environment.define(fun.name.lexeme(), new LoxFunction(fun, environment));
        return null;
    }

    @Nullable
    @Override
    public Object visitReturn(Stmt.Return ret) {
        if (ret.value.isPresent()) {
            var value = eval(ret.value.get());
            throw new Return(value);
        }
        throw new Return(null);
    }

    @Nullable
    @Override
    public Object visitClass(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme(), null);
        var methodsBuilder = ImmutableMap.<String, LoxFunction>builder();
        for (var method : stmt.methods) {
            var function = new LoxFunction(method, environment);
            methodsBuilder.put(method.name.lexeme(), function);
        }
        var klass = new LoxClass(stmt.name.lexeme(), methodsBuilder.build());
        environment.assign(stmt.name, klass);
        return null;
    }

    @Nullable
    @Override
    public Object visitGet(Expr.Get expr) {
        var object = eval(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "only instances have properties");
    }

    @Nullable
    @Override
    public Object visitSet(Expr.Set expr) {
        var object = eval(expr.object);
        if (object instanceof LoxInstance) {
            var value = eval(expr.value);
            ((LoxInstance) object).set(expr.name, value);
            return value;
        }
        throw new RuntimeError(expr.name, "only instances have properties");
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

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }
}
