package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Resolver implements Visitor<Void> {
    private final Interpreter interpreter;
    private final List<Map<String, Boolean>> scopes = new ArrayList<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    private enum FunctionType {
        NONE,
        METHOD,
        FUNCTION,
        INITIALIZER
    }

    private enum ClassType {
        NONE,
        SUBCLASS,
        CLASS
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void beginScope() {
        scopes.add(new HashMap<>());
    }

    private void endScope() {
        scopes.remove(scopes.size() - 1);
    }

    private Map<String, Boolean> lastScope() {
        return scopes.get(scopes.size() - 1);
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        var scope = lastScope();
        if (scope.containsKey(name.lexeme())) {
            Lox.error(name, "variable with this name already declared in this scope");
        }
        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        lastScope().put(name.lexeme(), true);
    }

    void resolve(ImmutableList<Stmt> stmts) {
        for (var stmt : stmts) {
            resolve(stmt);
        }
    }

    private void resolve(AstNode node) {
        node.accept(this);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType functionType) {
        var enclosing = currentFunction;
        currentFunction = functionType;
        beginScope();
        for (var param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosing;
    }

    @Override
    public Void visitBinary(Expr.Binary binary) {
        resolve(binary.left);
        resolve(binary.right);
        return null;
    }

    @Override
    public Void visitUnary(Expr.Unary unary) {
        resolve(unary.right);
        return null;
    }

    @Override
    public Void visitGrouping(Expr.Grouping grouping) {
        resolve(grouping.expression);
        return null;
    }

    @Override
    public Void visitLiteral(Expr.Literal literal) {
        return null;
    }

    @Override
    public Void visitExpression(Stmt.Expression expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print print) {
        resolve(print.expression);
        return null;
    }

    @Override
    public Void visitVar(Stmt.VariableDeclaration variableDeclaration) {
        declare(variableDeclaration.name);
        variableDeclaration.initializer.ifPresent(this::resolve);
        define(variableDeclaration.name);
        return null;
    }

    @Override
    public Void visitVariableReference(Expr.VariableReference variableReference) {
        if (!scopes.isEmpty()) {
            var scope = scopes.get(scopes.size() - 1);
            if (scope.containsKey(variableReference.name.lexeme()) && !scope.get(variableReference.name.lexeme())) {
                Lox.error(variableReference.name, "variable initialized cannot contain its own name");
            }
        }
        resolveLocal(variableReference, variableReference.name);
        return null;
    }

    @Override
    public Void visitAssignment(Expr.Assignment assignment) {
        resolve(assignment.expression);
        resolveLocal(assignment, assignment.name);
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIf(Stmt.If anIf) {
        resolve(anIf.condition);
        resolve(anIf.thenBranch);
        anIf.elseBranch.ifPresent(this::resolve);
        return null;
    }

    @Override
    public Void visitLogical(Expr.Logical logical) {
        resolve(logical.left);
        resolve(logical.right);
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While aWhile) {
        resolve(aWhile.condition);
        resolve(aWhile.body);
        return null;
    }

    @Override
    public Void visitCall(Expr.Call call) {
        resolve(call.callee);
        for (var arg : call.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitFunction(Stmt.Function function) {
        declare(function.name);
        define(function.name);
        resolveFunction(function, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturn(Stmt.Return aReturn) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(aReturn.keyword, "cannot return from top-level code");
        }
        if (currentFunction == FunctionType.INITIALIZER) {
            Lox.error(aReturn.keyword, "cannot return from initializer");
        }
        aReturn.value.ifPresent(this::resolve);
        return null;
    }

    @Nullable
    @Override
    public Void visitClass(Stmt.Class stmt) {
        var enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass.isPresent()) {
            if (stmt.name.lexeme().equals(stmt.superclass.get().name.lexeme())) {
                Lox.error(stmt.superclass.get().name, "cannot inherit from itself");
            }
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass.get());
        }

        if (stmt.superclass.isPresent()) {
            beginScope();
            scopes.get(scopes.size() - 1).put("super", true);
        }

        beginScope();
        lastScope().put("this", true);
        for (var method : stmt.methods) {
            var declaration = FunctionType.METHOD;
            if (method.name.lexeme().equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }
        endScope();

        if (stmt.superclass.isPresent()) {
            endScope();
        }

        currentClass = enclosingClass;
        return null;
    }

    @Nullable
    @Override
    public Void visitGet(Expr.Get get) {
        resolve(get.object);
        return null;
    }

    @Nullable
    @Override
    public Void visitSet(Expr.Set set) {
        resolve(set.value);
        resolve(set.object);
        return null;
    }

    @Nullable
    @Override
    public Void visitThis(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "cannot use 'this' outside of class");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Nullable
    @Override
    public Void visitSuper(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "cannot use 'super' outside of class");
            return null;
        }
        if (currentClass != ClassType.SUBCLASS) {
            Lox.error(expr.keyword, "cannot use 'super' in a class with no superclass");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }
}
