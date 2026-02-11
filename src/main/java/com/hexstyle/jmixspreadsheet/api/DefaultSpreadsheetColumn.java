package com.hexstyle.jmixspreadsheet.api;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Default immutable implementation of {@link SpreadsheetColumn}.
 *
 * @param <E> the entity type
 */
public class DefaultSpreadsheetColumn<E> implements SpreadsheetColumn<E> {

    private final String id;
    private final String header;
    private final Function<E, Object> valueProvider;
    private final BiConsumer<E, Object> setter;
    private final Function<Object, String> formatter;
    private final Integer width;
    private final Alignment alignment;
    private final boolean editable;

    public DefaultSpreadsheetColumn(
            String id,
            String header,
            Function<E, Object> valueProvider,
            BiConsumer<E, Object> setter,
            Function<Object, String> formatter,
            Integer width,
            Alignment alignment,
            boolean editable) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Column ID cannot be null or empty");
        }
        if (header == null) {
            throw new IllegalArgumentException("Column header cannot be null");
        }
        if (valueProvider == null) {
            throw new IllegalArgumentException("Value provider cannot be null");
        }

        this.id = id;
        this.header = header;
        this.valueProvider = valueProvider;
        this.setter = setter;
        this.formatter = formatter;
        this.width = width;
        this.alignment = alignment;
        this.editable = editable;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public Function<E, Object> getValueProvider() {
        return valueProvider;
    }

    @Override
    public BiConsumer<E, Object> getSetter() {
        return setter;
    }

    @Override
    public Function<Object, String> getFormatter() {
        return formatter;
    }

    @Override
    public Integer getWidth() {
        return width;
    }

    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }
}
