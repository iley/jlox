package com.istrukov.jlox;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public class Return extends RuntimeException {
    @Nullable
    final Object value;

    Return(@Nullable Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
