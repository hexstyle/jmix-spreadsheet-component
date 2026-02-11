package com.hexstyle.jmixspreadsheet.ui;

import java.util.List;
import java.util.function.Function;

public final class SpreadsheetComponentOptions<E> {

    private final List<StyleRule<E>> styleRules;
    private final Function<StyleToken, String> styleProvider;

    public SpreadsheetComponentOptions(List<StyleRule<E>> styleRules,
                                       Function<StyleToken, String> styleProvider) {
        this.styleRules = styleRules == null ? List.of() : List.copyOf(styleRules);
        this.styleProvider = styleProvider == null ? token -> null : styleProvider;
    }

    public List<StyleRule<E>> getStyleRules() {
        return styleRules;
    }

    public Function<StyleToken, String> getStyleProvider() {
        return styleProvider;
    }

    public static <E> SpreadsheetComponentOptions<E> empty() {
        return new SpreadsheetComponentOptions<>(List.of(), token -> null);
    }
}
