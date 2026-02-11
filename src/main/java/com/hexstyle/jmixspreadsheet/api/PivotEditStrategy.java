package com.hexstyle.jmixspreadsheet.api;

import java.util.List;
import java.util.Map;

/**
 * Defines the strategy for handling edits to pivot table cells.
 * <p>
 * When a user edits a pivot cell (which represents an aggregated value),
 * this strategy determines which source entities should be modified and
 * how the new value should be distributed among them.
 *
 * @param <E> the entity type
 */
public interface PivotEditStrategy<E> {

    /**
     * Determines which entities should be modified when a pivot cell is edited,
     * and what values should be set on each entity.
     * <p>
     * The pivot context identifies which entities contribute to the edited cell,
     * based on the row and column axis values at that cell position.
     *
     * @param pivotContext the context identifying the pivot cell and its contributing entities
     * @param newValue the new value entered by the user
     * @return a map of entities to their new property values.
     *         The map keys are the entities to modify, and the map values are
     *         maps from property names to new values.
     */
    Map<E, Map<String, Object>> determineEdits(PivotEditContext<E> pivotContext, Object newValue);

    /**
     * Context information for a pivot cell edit operation.
     * <p>
     * Provides access to the entities that contribute to the pivot cell being edited.
     *
     * @param <E> the entity type
     */
    interface PivotEditContext<E> {
        /**
         * Returns the entities that contribute to this pivot cell.
         * <p>
         * These are the source entities that match the row and column axis values
         * at the edited cell position.
         *
         * @return the list of contributing entities
         */
        List<E> getContributingEntities();

        /**
         * Returns the row axis values at this cell position.
         *
         * @return map from axis ID to axis value
         */
        Map<String, Object> getRowAxisValues();

        /**
         * Returns the column axis values at this cell position.
         *
         * @return map from axis ID to axis value
         */
        Map<String, Object> getColumnAxisValues();

        /**
         * Returns the measure ID for the edited cell.
         *
         * @return the measure ID
         */
        String getMeasureId();

        /**
         * Returns the current aggregated value at this cell before editing.
         *
         * @return the current value
         */
        Object getCurrentValue();
    }
}
