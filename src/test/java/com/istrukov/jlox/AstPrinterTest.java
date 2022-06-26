package com.istrukov.jlox;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

public class AstPrinterTest {
    @Test
    public void testOnePlusTwo() {
        var one = new Expr.Literal(new Token.Literal(1.0));
        var two = new Expr.Literal(new Token.Literal(2.0));
        var plus = new Token(TokenType.PLUS, "+", Optional.empty(), 1);
        var onePlusTwo = new Expr.Binary(one, plus, two);
        var astPrinter = new AstPrinter();
        assertEquals("(+ 1.0 2.0)", astPrinter.print(onePlusTwo));
    }
}
