package com.hexstyle.jmixspreadsheet.ui;

import java.util.function.Predicate;

public final class StyleRule<E> {

    private final StyleToken token;
    private final Predicate<CellStyleContext<E>> predicate;

    public StyleRule(StyleToken token, Predicate<CellStyleContext<E>> predicate) {
        if (token == null) {
            throw new IllegalArgumentException("Style token cannot be null");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }
        this.token = token;
        this.predicate = predicate;
    }

    public StyleToken getToken() {
        return token;
    }

    public boolean matches(CellStyleContext<E> context) {
        return predicate.test(context);
    }

    public static <E> StyleRule<E> of(StyleToken token, Predicate<CellStyleContext<E>> predicate) {
        return new StyleRule<>(token, predicate);
    }
}
