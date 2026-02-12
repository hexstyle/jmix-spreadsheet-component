package com.example.scm.portbalance.style;

import java.util.function.Predicate;

public record StyleRule(Predicate<CellContext> predicate, StyleToken token) {

    public boolean matches(CellContext context) {
        return predicate.test(context);
    }
}
