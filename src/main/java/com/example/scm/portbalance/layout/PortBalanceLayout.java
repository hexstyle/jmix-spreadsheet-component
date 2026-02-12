package com.example.scm.portbalance.layout;

import com.example.scm.portbalance.aggregate.PortBalanceCell;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;

import java.util.List;

public class PortBalanceLayout implements SpreadsheetLayout<PortBalanceCell> {

    private final int rowCount;
    private final int columnCount;
    private final List<CellBinding<PortBalanceCell>> cellBindings;
    private final List<MergedRegion> mergedRegions;
    private final List<RowGroup> rowGroups;

    public PortBalanceLayout(int rowCount,
                             int columnCount,
                             List<CellBinding<PortBalanceCell>> cellBindings,
                             List<MergedRegion> mergedRegions,
                             List<RowGroup> rowGroups) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cellBindings = cellBindings;
        this.mergedRegions = mergedRegions;
        this.rowGroups = rowGroups;
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
    public List<CellBinding<PortBalanceCell>> getCellBindings() {
        return cellBindings;
    }

    @Override
    public List<MergedRegion> getMergedRegions() {
        return mergedRegions;
    }

    @Override
    public List<RowGroup> getRowGroups() {
        return rowGroups;
    }
}
