package com.hexstyle.jmixspreadsheet.layout;

import java.util.List;

/**
 * Represents the layout structure of a spreadsheet.
 * <p>
 * This structure defines how entities are mapped to spreadsheet cells,
 * including row/column definitions, merged regions, and cell bindings.
 *
 * @param <E> the entity type
 */
public interface SpreadsheetLayout<E> {

    /**
     * Returns the number of rows in this layout.
     *
     * @return the row count
     */
    int getRowCount();

    /**
     * Returns the number of columns in this layout.
     *
     * @return the column count
     */
    int getColumnCount();

    /**
     * Returns the list of cell bindings in this layout.
     *
     * @return the list of cell bindings
     */
    List<CellBinding<E>> getCellBindings();

    /**
     * Returns the list of merged regions in this layout.
     *
     * @return the list of merged regions
     */
    List<MergedRegion> getMergedRegions();

    /**
     * Returns the list of row groups in this layout.
     *
     * @return the list of row groups
     */
    List<RowGroup> getRowGroups();
}
