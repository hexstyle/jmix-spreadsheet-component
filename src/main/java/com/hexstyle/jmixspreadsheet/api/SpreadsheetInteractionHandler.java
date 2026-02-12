package com.hexstyle.jmixspreadsheet.api;

import java.time.Instant;

/**
 * Handles user interactions with spreadsheet cells.
 * <p>
 * Implementations can react to cell clicks, double-clicks, selections,
 * and other user interactions, providing custom behavior for spreadsheet cells.
 *
 * @param <E> the entity type
 */
public interface SpreadsheetInteractionHandler<E> {

    /**
     * Called when a cell is clicked.
     *
     * @param context the interaction context containing cell and entity information
     */
    default void onCellClick(InteractionContext<E> context) {
        // Default implementation does nothing
    }

    /**
     * Called when a cell is double-clicked.
     *
     * @param context the interaction context containing cell and entity information
     */
    default void onCellDoubleClick(InteractionContext<E> context) {
        // Default implementation does nothing
    }

    /**
     * Called when cell selection changes.
     *
     * @param context the interaction context containing selection information
     */
    default void onSelectionChange(InteractionContext<E> context) {
        // Default implementation does nothing
    }

    /**
     * Called after a cell edit attempt is processed.
     * <p>
     * This callback is fired for both successful and rejected edits.
     * Use {@link InteractionContext#isEditSuccessful()} and
     * {@link InteractionContext#getEditError()} to inspect the result.
     *
     * @param context the interaction context containing edit details
     */
    default void onCellEdit(InteractionContext<E> context) {
        // Default implementation does nothing
    }

    /**
     * Context information for a spreadsheet interaction.
     * <p>
     * Provides access to the cell location, entity data, and selection state.
     *
     * @param <E> the entity type
     */
    interface InteractionContext<E> {
        /**
         * Returns the row index of the interacted cell.
         *
         * @return the row index (0-based)
         */
        int getRowIndex();

        /**
         * Returns the column index of the interacted cell.
         *
         * @return the column index (0-based)
         */
        int getColumnIndex();

        /**
         * Returns the entity associated with this cell, if any.
         * <p>
         * For flat tables, this returns the entity for the row.
         * For pivot tables, this may return {@code null} for aggregate cells.
         *
         * @return the entity, or {@code null} if not applicable
         */
        E getEntity();

        /**
         * Returns whether this cell is part of a pivot table.
         *
         * @return {@code true} if this is a pivot cell, {@code false} otherwise
         */
        boolean isPivotCell();

        /**
         * Returns the selected row indices.
         * <p>
         * Only applicable for selection change events.
         *
         * @return the array of selected row indices, or empty array if not applicable
         */
        int[] getSelectedRows();

        /**
         * Returns the selected column indices.
         * <p>
         * Only applicable for selection change events.
         *
         * @return the array of selected column indices, or empty array if not applicable
         */
        int[] getSelectedColumns();

        /**
         * Returns the edit event timestamp.
         *
         * @return timestamp for edit events, or {@code null} for non-edit interactions
         */
        default Instant getEditTimestamp() {
            return null;
        }

        /**
         * Returns the cell value before edit.
         *
         * @return old value for edit events, or {@code null} if not applicable
         */
        default Object getOldValue() {
            return null;
        }

        /**
         * Returns the value requested by the user.
         *
         * @return requested new value for edit events, or {@code null} if not applicable
         */
        default Object getNewValue() {
            return null;
        }

        /**
         * Returns whether the edit was applied successfully.
         *
         * @return {@code true} for successful edit, {@code false} for rejected/failed edit,
         * or {@code null} if not applicable
         */
        default Boolean isEditSuccessful() {
            return null;
        }

        /**
         * Returns error text when edit failed.
         *
         * @return error text for failed edits, or {@code null}
         */
        default String getEditError() {
            return null;
        }
    }
}
