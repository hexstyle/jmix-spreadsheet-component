package com.example.scm.portbalance.layout;

import com.hexstyle.jmixspreadsheet.layout.MergedRegion;

public class PortBalanceMergedRegion implements MergedRegion {

    private final int firstRow;
    private final int lastRow;
    private final int firstColumn;
    private final int lastColumn;

    public PortBalanceMergedRegion(int firstRow, int lastRow, int firstColumn, int lastColumn) {
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
    }

    @Override
    public int getFirstRow() {
        return firstRow;
    }

    @Override
    public int getLastRow() {
        return lastRow;
    }

    @Override
    public int getFirstColumn() {
        return firstColumn;
    }

    @Override
    public int getLastColumn() {
        return lastColumn;
    }
}
