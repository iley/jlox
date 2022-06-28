package com.istrukov.jlox;

import javax.annotation.Nullable;

abstract class AstNode {
    @Nullable
    abstract <R> R accept(Visitor<R> visitor);
}
