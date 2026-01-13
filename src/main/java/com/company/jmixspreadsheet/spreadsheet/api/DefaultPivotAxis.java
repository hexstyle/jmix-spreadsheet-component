package com.company.jmixspreadsheet.spreadsheet.api;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Default immutable implementation of {@link PivotAxis}.
 *
 * @param <E> the entity type
 */
public class DefaultPivotAxis<E> implements PivotAxis<E> {

    private final String id;
    private final Function<E, Object> keyProvider;
    private final Comparator<Object> comparator;
    private final RenderMode renderMode;

    public DefaultPivotAxis(
            String id,
            Function<E, Object> keyProvider,
            Comparator<Object> comparator,
            RenderMode renderMode) {
        this.id = id;
        this.keyProvider = keyProvider;
        this.comparator = comparator;
        this.renderMode = renderMode;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Function<E, Object> getKeyProvider() {
        return keyProvider;
    }

    @Override
    public Comparator<Object> getComparator() {
        return comparator;
    }

    @Override
    public RenderMode getRenderMode() {
        return renderMode;
    }
}
