package com.company.jmixspreadsheet.spreadsheet.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Defines the pivot table configuration for a spreadsheet.
 * <p>
 * A pivot table reorganizes and summarizes entity data by grouping on axes
 * and aggregating measures. This interface defines the structure of the pivot.
 * <p>
 * Pivot configurations are immutable once created.
 *
 * @param <E> the entity type
 */
public interface SpreadsheetPivot<E> {

    /**
     * Returns the axes used for row grouping.
     * <p>
     * The order of axes in the list determines the hierarchy level,
     * with earlier axes being higher in the hierarchy.
     *
     * @return the list of row axes
     */
    List<PivotAxis<E>> getRowAxes();

    /**
     * Returns the axes used for column grouping.
     * <p>
     * The order of axes in the list determines the hierarchy level,
     * with earlier axes being higher in the hierarchy.
     *
     * @return the list of column axes
     */
    List<PivotAxis<E>> getColumnAxes();

    /**
     * Returns the measures to aggregate in the pivot table.
     *
     * @return the list of measures
     */
    List<PivotMeasure<E>> getMeasures();

    /**
     * Returns the strategy for handling edits to pivot cells.
     *
     * @return the edit strategy, or {@code null} if pivot cells are read-only
     */
    PivotEditStrategy<E> getEditStrategy();

    /**
     * Returns a supplier that provides a complete set of row axis values for the first row axis.
     * <p>
     * When provided, this supplier returns a list of all possible values that should appear
     * as rows in the pivot table, even if no entities exist for those values. This allows
     * generating rows from an external data source (e.g., all dates in a date range).
     * <p>
     * The returned values should match the type returned by the first row axis's key provider.
     * If not specified, rows are generated only from values that exist in the entity data.
     *
     * @return optional supplier of complete row axis values, or empty if not specified
     */
    default Optional<Supplier<List<Object>>> getRowCompletion() {
        return Optional.empty();
    }

    /**
     * Returns a supplier that provides a complete set of column axis values for the first column axis.
     * <p>
     * When provided, this supplier returns a list of all possible values that should appear
     * as columns in the pivot table, even if no entities exist for those values. This allows
     * generating columns from an external data source.
     * <p>
     * The returned values should match the type returned by the first column axis's key provider.
     * If not specified, columns are generated only from values that exist in the entity data.
     *
     * @return optional supplier of complete column axis values, or empty if not specified
     */
    default Optional<Supplier<List<Object>>> getColumnCompletion() {
        return Optional.empty();
    }
}
