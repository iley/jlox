package com.istrukov.jlox;

import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    private static final boolean printTokens = false;
    private static final boolean printAst = false;

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    private static Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runPrompt();
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            System.err.println("Usage: jlox [SCRIPT]");
            System.exit(1);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError || hadRuntimeError) System.exit(1);
    }

    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);
        while (true) {
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String script) {
        var scanner = new Scanner(script);
        var tokens = ImmutableList.copyOf(scanner.scanTokens());
        if (printTokens) {
            for (var token : tokens) {
                System.out.printf("%s ", token);
            }
            System.out.println();
        }
        var parser = new Parser(tokens);
        var program = parser.parse();
        if (hadError) {
            return;
        }
        interpreter.interpret(program);
    }

    static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), String.format(" at '%s'", token.lexeme()), message);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.printf("[line %d] %s\n", error.token.line(), error.getMessage());
        hadRuntimeError = true;
    }

    static void report(int line, String where, String message) {
        System.err.printf("[line %d] Error%s: %s\n", line, where, message);
        hadError = true;
    }
}