package com.hexstyle.jmixspreadsheet.layout;

public class RowGroup {

    private final int startRow;
    private final int endRow;
    private final boolean collapsed;
    private final String label;

    public RowGroup(int startRow, int endRow, boolean collapsed, String label) {
        this.startRow = startRow;
        this.endRow = endRow;
        this.collapsed = collapsed;
        this.label = label;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public String getLabel() {
        return label;
    }
}
