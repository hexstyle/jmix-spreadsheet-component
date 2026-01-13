package com.company.jmixspreadsheet.spreadsheet.diff;

import com.company.jmixspreadsheet.spreadsheet.index.LayoutIndex.CellRef;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link LayoutDelta} for flat tables.
 * <p>
 * This implementation supports only cell updates (cellsToUpdate).
 * For flat tables without grouping or sorting, rows are never inserted or removed,
 * and cells are updated rather than cleared.
 */
public class DefaultLayoutDelta implements LayoutDelta {

    private final Set<CellRef> cellsToUpdate;

    /**
     * Creates a new layout delta with the given cells to update.
     *
     * @param cellsToUpdate the set of cell references to update
     */
    public DefaultLayoutDelta(Set<CellRef> cellsToUpdate) {
        this.cellsToUpdate = cellsToUpdate != null 
                ? new HashSet<>(cellsToUpdate) 
                : new HashSet<>();
    }

    /**
     * Creates an empty layout delta (no changes).
     *
     * @return an empty layout delta
     */
    public static DefaultLayoutDelta empty() {
        return new DefaultLayoutDelta(Collections.emptySet());
    }

    @Override
    public Set<CellRef> getCellsToUpdate() {
        return Collections.unmodifiableSet(cellsToUpdate);
    }

    @Override
    public Set<CellRef> getCellsToClear() {
        // For flat tables, cells are updated rather than cleared
        return Collections.emptySet();
    }

    @Override
    public List<Integer> getRowsToInsert() {
        // For flat tables without grouping/sorting, rows are never inserted
        return Collections.emptyList();
    }

    @Override
    public List<Integer> getRowsToRemove() {
        // For flat tables without grouping/sorting, rows are never removed
        return Collections.emptyList();
    }
}
