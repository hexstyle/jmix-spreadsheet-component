package com.hexstyle.jmixspreadsheet.api;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Represents a pivot axis definition.
 * <p>
 * A pivot axis defines how entities are grouped and organized in a pivot table,
 * either as row headers or column headers. Axes are immutable once created.
 *
 * @param <E> the entity type
 */
public interface PivotAxis<E> {

    /**
     * Returns the unique identifier for this axis.
     *
     * @return the axis ID
     */
    String getId();

    /**
     * Returns the function that extracts the grouping key from an entity.
     * <p>
     * Entities with the same key value will be grouped together on this axis.
     *
     * @return the key provider function
     */
    Function<E, Object> getKeyProvider();

    /**
     * Returns the comparator used to sort axis items.
     * <p>
     * If not specified, natural ordering is used.
     *
     * @return the comparator, or {@code null} to use natural ordering
     */
    Comparator<Object> getComparator();

    /**
     * Returns the rendering mode for this axis.
     *
     * @return the render mode
     */
    RenderMode getRenderMode();

    /**
     * Defines how axis headers are rendered in the spreadsheet.
     */
    enum RenderMode {
        /**
         * Headers are displayed as regular cells with no merging.
         */
        HEADER,

        /**
         * Headers are merged across grouped cells.
         */
        MERGED,

        /**
         * Headers are displayed with outline grouping (expand/collapse).
         */
        OUTLINE
    }
}
