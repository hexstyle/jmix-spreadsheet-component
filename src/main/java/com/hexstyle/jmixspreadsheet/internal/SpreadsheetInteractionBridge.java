package com.hexstyle.jmixspreadsheet.internal;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.util.CellReference;

import java.util.Set;

public final class SpreadsheetInteractionBridge {

    private SpreadsheetInteractionBridge() {
    }

    public static <E> void handleSelectionChange(Spreadsheet.SelectionChangeEvent event,
                                                 SpreadsheetInteractionHandler<E> handler,
                                                 LayoutIndex<E> layoutIndex) {
        if (event == null || handler == null) {
            return;
        }
        CellReference reference = event.getSelectedCellReference();
        if (reference == null) {
            return;
        }
        handleCellClick(reference.getRow(), reference.getCol(),
                event.getAllSelectedCells(), handler, layoutIndex);
    }

    public static <E> void handleCellClick(int row,
                                           int column,
                                           Set<CellReference> selectedCells,
                                           SpreadsheetInteractionHandler<E> handler,
                                           LayoutIndex<E> layoutIndex) {
        if (handler == null) {
            return;
        }
        CellBinding<E> binding = null;
        if (layoutIndex != null) {
            binding = layoutIndex.getCellBinding(new CellRefImpl(row, column));
        }
        InteractionContextImpl<E> context =
                new InteractionContextImpl<>(row, column, binding, selectedCells);
        handler.onCellClick(context);
    }

    private static final class CellRefImpl implements LayoutIndex.CellRef {
        private final int row;
        private final int column;

        private CellRefImpl(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public int getRow() {
            return row;
        }

        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LayoutIndex.CellRef)) return false;
            LayoutIndex.CellRef that = (LayoutIndex.CellRef) o;
            return row == that.getRow() && column == that.getColumn();
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, column);
        }
    }
}
