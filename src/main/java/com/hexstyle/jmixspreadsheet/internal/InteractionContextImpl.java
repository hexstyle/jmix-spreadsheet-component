package com.hexstyle.jmixspreadsheet.internal;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import org.apache.poi.ss.util.CellReference;

import java.util.Set;

public final class InteractionContextImpl<E>
        implements SpreadsheetInteractionHandler.InteractionContext<E> {

    private final int rowIndex;
    private final int columnIndex;
    private final CellBinding<E> binding;
    private final int[] selectedRows;
    private final int[] selectedColumns;

    public InteractionContextImpl(int rowIndex,
                                  int columnIndex,
                                  CellBinding<E> binding,
                                  Set<CellReference> selectedCells) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.binding = binding;
        this.selectedRows = resolveSelectedRows(selectedCells);
        this.selectedColumns = resolveSelectedColumns(selectedCells);
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public int getColumnIndex() {
        return columnIndex;
    }

    @Override
    public E getEntity() {
        return binding == null ? null : binding.getEntityRef();
    }

    @Override
    public boolean isPivotCell() {
        return binding != null && binding.getPivotContext() != null;
    }

    @Override
    public int[] getSelectedRows() {
        return selectedRows;
    }

    @Override
    public int[] getSelectedColumns() {
        return selectedColumns;
    }

    public CellBinding<E> getCellBinding() {
        return binding;
    }

    private static int[] resolveSelectedRows(Set<CellReference> selectedCells) {
        if (selectedCells == null || selectedCells.isEmpty()) {
            return new int[0];
        }
        return selectedCells.stream()
                .mapToInt(CellReference::getRow)
                .distinct()
                .sorted()
                .toArray();
    }

    private static int[] resolveSelectedColumns(Set<CellReference> selectedCells) {
        if (selectedCells == null || selectedCells.isEmpty()) {
            return new int[0];
        }
        return selectedCells.stream()
                .mapToInt(CellReference::getCol)
                .distinct()
                .sorted()
                .toArray();
    }
}
