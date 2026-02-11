package com.hexstyle.jmixspreadsheet.api;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a column definition in a spreadsheet table model.
 * <p>
 * Each column defines how entity data is displayed and edited in the spreadsheet.
 * Columns are immutable once created.
 *
 * @param <E> the entity type
 */
public interface SpreadsheetColumn<E> {

    /**
     * Returns the unique identifier for this column.
     *
     * @return the column ID
     */
    String getId();

    /**
     * Returns the header text displayed for this column.
     *
     * @return the header text
     */
    String getHeader();

    /**
     * Returns the function that extracts the value from an entity for this column.
     *
     * @return the value provider function
     */
    Function<E, Object> getValueProvider();

    /**
     * Returns the function that sets a value on an entity for this column.
     * <p>
     * This is optional - if not present, the column is read-only.
     *
     * @return the setter function, or {@code null} if the column is read-only
     */
    BiConsumer<E, Object> getSetter();

    /**
     * Returns the formatter function that converts the value to a string for display.
     * <p>
     * If not specified, the value's {@code toString()} method is used.
     *
     * @return the formatter function, or {@code null} to use default formatting
     */
    Function<Object, String> getFormatter();

    /**
     * Returns the preferred width of this column.
     *
     * @return the column width, or {@code null} to use default width
     */
    Integer getWidth();

    /**
     * Returns the text alignment for this column.
     *
     * @return the alignment, or {@code null} to use default alignment
     */
    Alignment getAlignment();

    /**
     * Returns whether this column is editable.
     *
     * @return {@code true} if editable, {@code false} otherwise
     */
    boolean isEditable();

    /**
     * Text alignment options for spreadsheet columns.
     */
    enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
