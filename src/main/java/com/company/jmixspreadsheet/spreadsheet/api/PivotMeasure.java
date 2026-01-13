package com.company.jmixspreadsheet.spreadsheet.api;

import java.util.function.Function;

/**
 * Represents a measure definition in a pivot table.
 * <p>
 * A measure defines how values are aggregated and displayed at pivot intersections.
 * Measures are immutable once created.
 *
 * @param <E> the entity type
 */
public interface PivotMeasure<E> {

    /**
     * Returns the unique identifier for this measure.
     *
     * @return the measure ID
     */
    String getId();

    /**
     * Returns the caption displayed for this measure.
     *
     * @return the measure caption
     */
    String getCaption();

    /**
     * Returns the function that extracts the value from an entity for aggregation.
     *
     * @return the value provider function
     */
    Function<E, Number> getValueProvider();

    /**
     * Returns the aggregation type used for this measure.
     *
     * @return the aggregation type
     */
    AggregationType getAggregation();

    /**
     * Returns the custom aggregation function, if aggregation type is CUSTOM.
     *
     * @return the custom aggregation function, or {@code null} if not applicable
     */
    Function<Iterable<Number>, Number> getCustomAggregation();

    /**
     * Defines the aggregation operation to apply to measure values.
     */
    enum AggregationType {
        /**
         * Sum of all values.
         */
        SUM,

        /**
         * Count of entities.
         */
        COUNT,

        /**
         * Average of all values.
         */
        AVG,

        /**
         * Custom aggregation function provided via {@link #getCustomAggregation()}.
         */
        CUSTOM
    }
}
