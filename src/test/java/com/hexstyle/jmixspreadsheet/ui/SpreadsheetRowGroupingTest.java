package com.hexstyle.jmixspreadsheet.ui;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.render.DefaultSpreadsheetRenderer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SpreadsheetRowGroupingTest {

    @Test
    void rendererAppliesRowGroups() {
        List<RowGroup> groups = List.of(
                new RowGroup(3, 5, true, "Group A"),
                new RowGroup(7, 8, false, "Group B")
        );
        SpreadsheetLayout<Object> layout = new TestLayout(groups);

        List<RowGroup> applied = new ArrayList<>();
        DefaultSpreadsheetRenderer.CellRenderer<Object> cellRenderer =
                new DefaultSpreadsheetRenderer.CellRenderer<>() {
                    @Override
                    public void clear(Object component) {
                    }

                    @Override
                    public void setCellValue(Object component, int row, int column, String value) {
                    }

                    @Override
                    public void setCellStyle(Object component, int row, int column, String style) {
                    }

                    @Override
                    public void mergeCells(Object component, int firstRow, int lastRow, int firstColumn, int lastColumn) {
                    }

                    @Override
                    public void groupRows(Object component, int startRow, int endRow, boolean collapsed) {
                        applied.add(new RowGroup(startRow, endRow, collapsed, null));
                    }
                };

        DefaultSpreadsheetRenderer<Object, Object> renderer = new DefaultSpreadsheetRenderer<>(cellRenderer);
        renderer.render(new Object(), layout);

        Assertions.assertThat(applied).hasSize(2);
        Assertions.assertThat(applied.get(0).getStartRow()).isEqualTo(3);
        Assertions.assertThat(applied.get(0).getEndRow()).isEqualTo(5);
        Assertions.assertThat(applied.get(0).isCollapsed()).isTrue();
        Assertions.assertThat(applied.get(1).getStartRow()).isEqualTo(7);
        Assertions.assertThat(applied.get(1).getEndRow()).isEqualTo(8);
        Assertions.assertThat(applied.get(1).isCollapsed()).isFalse();
    }

    private static final class TestLayout implements SpreadsheetLayout<Object> {
        private final List<RowGroup> rowGroups;

        private TestLayout(List<RowGroup> rowGroups) {
            this.rowGroups = rowGroups;
        }

        @Override
        public int getRowCount() {
            return 10;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public List<CellBinding<Object>> getCellBindings() {
            return List.of();
        }

        @Override
        public List<MergedRegion> getMergedRegions() {
            return List.of();
        }

        @Override
        public List<RowGroup> getRowGroups() {
            return rowGroups;
        }
    }
}
