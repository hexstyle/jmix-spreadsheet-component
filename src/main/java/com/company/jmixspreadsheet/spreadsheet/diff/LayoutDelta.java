package com.company.jmixspreadsheet.spreadsheet.diff;

import com.company.jmixspreadsheet.spreadsheet.index.LayoutIndex.CellRef;

import java.util.List;
import java.util.Set;

/**
 * Describes changes to the spreadsheet layout that need to be applied.
 * <p>
 * A layout delta specifies which cells to update, clear, or where rows
 * need to be inserted or removed.
 */
public interface LayoutDelta {

    /**
     * Returns the set of cells that need to be updated.
     *
     * @return the set of cell references to update
     */
    Set<CellRef> getCellsToUpdate();

    /**
     * Returns the set of cells that need to be cleared.
     *
     * @return the set of cell references to clear
     */
    Set<CellRef> getCellsToClear();

    /**
     * Returns the list of rows that need to be inserted.
     * <p>
     * This is rare and typically only occurs when structure changes significantly.
     *
     * @return the list of row indices where rows should be inserted, or empty list
     */
    List<Integer> getRowsToInsert();

    /**
     * Returns the list of rows that need to be removed.
     * <p>
     * This is rare and typically only occurs when structure changes significantly.
     *
     * @return the list of row indices where rows should be removed, or empty list
     */
    List<Integer> getRowsToRemove();
}
