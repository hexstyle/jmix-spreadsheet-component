package com.company.jmixspreadsheet.spreadsheet.index;

import com.company.jmixspreadsheet.spreadsheet.layout.CellBinding;

import java.util.Set;

/**
 * Index that maps between entities and spreadsheet cells.
 * <p>
 * The layout index maintains bidirectional mappings:
 * - entity keys → cell references
 * - cell references → cell bindings
 * - pivot cells → contributing entity keys
 * <p>
 * This index is critical for incremental updates and interaction handling.
 *
 * @param <E> the entity type
 */
public interface LayoutIndex<E> {

    /**
     * Returns the set of cell references associated with an entity key.
     *
     * @param entityKey the entity key
     * @return the set of cell references, or empty set if not found
     */
    Set<CellRef> getCellRefs(Object entityKey);

    /**
     * Returns the cell binding for a given cell reference.
     *
     * @param cellRef the cell reference
     * @return the cell binding, or {@code null} if not found
     */
    CellBinding<E> getCellBinding(CellRef cellRef);

    /**
     * Returns the set of entity keys that contribute to a pivot cell.
     *
     * @param cellRef the pivot cell reference
     * @return the set of entity keys, or empty set if not found or not a pivot cell
     */
    Set<Object> getEntityKeys(CellRef cellRef);

    /**
     * References a spreadsheet cell by row and column index.
     */
    interface CellRef {
        /**
         * Returns the row index (0-based).
         *
         * @return the row index
         */
        int getRow();

        /**
         * Returns the column index (0-based).
         *
         * @return the column index
         */
        int getColumn();
    }
}
