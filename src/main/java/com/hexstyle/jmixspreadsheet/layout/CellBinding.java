package com.hexstyle.jmixspreadsheet.layout;

/**
 * Binds a spreadsheet cell to entity data or pivot context.
 * <p>
 * Cell bindings define the mapping between spreadsheet cells (by row/column index)
 * and the underlying entity data or pivot aggregation context.
 *
 * @param <E> the entity type
 */
public interface CellBinding<E> {

    /**
     * Returns the row index of the cell (0-based).
     *
     * @return the row index
     */
    int getRowIndex();

    /**
     * Returns the column index of the cell (0-based).
     *
     * @return the column index
     */
    int getColumnIndex();

    /**
     * Returns the value to display in the cell.
     *
     * @return the cell value
     */
    Object getValue();

    /**
     * Returns the style to apply to the cell.
     *
     * @return the cell style, or {@code null} to use default style
     */
    String getStyle();

    /**
     * Returns the entity reference, if this is a flat table cell.
     *
     * @return the entity, or {@code null} if this is a pivot cell
     */
    E getEntityRef();

    /**
     * Returns the pivot context, if this is a pivot table cell.
     *
     * @return the pivot context, or {@code null} if this is not a pivot cell
     */
    PivotContext getPivotContext();

    /**
     * Pivot context information for pivot cells.
     */
    interface PivotContext {
        // Pivot context details
    }
}
