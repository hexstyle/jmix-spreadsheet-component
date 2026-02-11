package com.hexstyle.jmixspreadsheet.api;

import java.util.List;
import java.util.Optional;

/**
 * Defines the table model for a spreadsheet view.
 * <p>
 * The table model is a declarative configuration that describes how entity data
 * should be displayed in the spreadsheet, including columns, grouping, filtering,
 * sorting, and optional pivot configuration.
 * <p>
 * Table models are immutable once created.
 *
 * @param <E> the entity type
 */
public interface SpreadsheetTableModel<E> {

    /**
     * Returns the entity class for this table model.
     *
     * @return the entity class
     */
    Class<E> getEntityClass();

    /**
     * Returns the list of columns to display in the spreadsheet.
     *
     * @return the list of columns
     */
    List<SpreadsheetColumn<E>> getColumns();

    /**
     * Returns the grouping configuration.
     * <p>
     * The grouping defines how entities are grouped by column values.
     * If not specified, entities are displayed in a flat list.
     *
     * @return the grouping configuration, or {@code null} if no grouping is applied
     */
    Grouping getGrouping();

    /**
     * Returns the filter condition.
     * <p>
     * The filter defines which entities should be displayed.
     * If not specified, all entities are displayed.
     *
     * @return the filter condition, or {@code null} if no filter is applied
     */
    Object getFilter();

    /**
     * Returns the sort configuration.
     * <p>
     * The sort defines the order in which entities are displayed.
     * If not specified, entities are displayed in their natural order.
     *
     * @return the sort configuration, or {@code null} if no sorting is applied
     */
    Sort getSort();

    /**
     * Returns the pivot table configuration, if any.
     *
     * @return the pivot configuration, or empty if this is a flat table
     */
    Optional<SpreadsheetPivot<E>> getPivot();

    /**
     * Returns the interaction handler for user interactions with cells.
     *
     * @return the interaction handler, or {@code null} if no custom handler is configured
     */
    SpreadsheetInteractionHandler<E> getInteractionHandler();

    /**
     * Defines grouping configuration for the spreadsheet.
     */
    interface Grouping {
        /**
         * Returns the column IDs used for grouping.
         * <p>
         * The order of column IDs determines the grouping hierarchy,
         * with earlier columns being higher in the hierarchy.
         *
         * @return the list of column IDs to group by
         */
        List<String> getColumnIds();

        /**
         * Returns whether grouped rows are expanded by default.
         *
         * @return {@code true} if expanded, {@code false} if collapsed
         */
        boolean isExpandedByDefault();
    }

    /**
     * Defines sort configuration for the spreadsheet.
     */
    interface Sort {
        /**
         * Returns the sort order specifications.
         *
         * @return the list of sort orders
         */
        List<SortOrder> getOrders();

        /**
         * Defines a single sort order specification.
         */
        interface SortOrder {
            /**
             * Returns the column ID to sort by.
             *
             * @return the column ID
             */
            String getColumnId();

            /**
             * Returns the sort direction.
             *
             * @return the sort direction
             */
            Direction getDirection();

            /**
             * Sort direction.
             */
            enum Direction {
                ASCENDING,
                DESCENDING
            }
        }
    }
}
