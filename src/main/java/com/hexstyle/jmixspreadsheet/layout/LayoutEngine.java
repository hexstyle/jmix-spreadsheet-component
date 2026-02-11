package com.hexstyle.jmixspreadsheet.layout;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;

/**
 * Builds the layout structure for a spreadsheet based on a table model and data.
 * <p>
 * The layout engine maps entity data to spreadsheet cells, rows, and columns,
 * producing a layout structure that can be rendered by the renderer.
 *
 * @param <E> the entity type
 */
public interface LayoutEngine<E> {

    /**
     * Builds a layout structure from the given model and entities.
     *
     * @param model the table model defining the structure
     * @param entities the entities to lay out
     * @return the layout structure
     */
    SpreadsheetLayout<E> buildLayout(SpreadsheetTableModel<E> model, Iterable<E> entities);
}
