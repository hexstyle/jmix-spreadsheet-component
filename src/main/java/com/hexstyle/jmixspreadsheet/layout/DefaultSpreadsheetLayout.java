package com.hexstyle.jmixspreadsheet.layout;

import java.util.Collections;
import java.util.List;

/**
 * Default immutable implementation of {@link SpreadsheetLayout}.
 *
 * @param <E> the entity type
 */
public class DefaultSpreadsheetLayout<E> implements SpreadsheetLayout<E> {

    private final int rowCount;
    private final int columnCount;
    private final List<CellBinding<E>> cellBindings;
    private final List<MergedRegion> mergedRegions;
    private final List<RowGroup> rowGroups;

    public DefaultSpreadsheetLayout(int rowCount,
                                    int columnCount,
                                    List<CellBinding<E>> cellBindings,
                                    List<MergedRegion> mergedRegions,
                                    List<RowGroup> rowGroups) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cellBindings = cellBindings == null ? List.of() : List.copyOf(cellBindings);
        this.mergedRegions = mergedRegions == null ? List.of() : List.copyOf(mergedRegions);
        this.rowGroups = rowGroups == null ? List.of() : List.copyOf(rowGroups);
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public List<CellBinding<E>> getCellBindings() {
        return Collections.unmodifiableList(cellBindings);
    }

    @Override
    public List<MergedRegion> getMergedRegions() {
        return Collections.unmodifiableList(mergedRegions);
    }

    @Override
    public List<RowGroup> getRowGroups() {
        return Collections.unmodifiableList(rowGroups);
    }
}
