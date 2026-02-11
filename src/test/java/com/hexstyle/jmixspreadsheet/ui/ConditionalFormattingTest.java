package com.hexstyle.jmixspreadsheet.ui;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.DefaultCellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.render.DefaultSpreadsheetRenderer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class ConditionalFormattingTest {

    @Test
    void rendererAppliesConditionalStylesAndCachesTokens() {
        List<CellBinding<Object>> bindings = List.of(
                new DefaultCellBinding<>(0, 0, -5, "base", null),
                new DefaultCellBinding<>(1, 0, -2, null, null)
        );
        SpreadsheetLayout<Object> layout = new TestLayout(bindings);

        Map<StyleToken, Integer> calls = new EnumMap<>(StyleToken.class);
        Function<StyleToken, String> provider = token -> {
            calls.put(token, calls.getOrDefault(token, 0) + 1);
            if (token == StyleToken.NEGATIVE) {
                return "neg-style";
            }
            if (token == StyleToken.HIGHLIGHT) {
                return "hl-style";
            }
            return null;
        };

        List<StyleRule<Object>> rules = List.of(
                new StyleRule<>(StyleToken.NEGATIVE,
                        context -> context.getValue() instanceof Number
                                && ((Number) context.getValue()).doubleValue() < 0),
                new StyleRule<>(StyleToken.HIGHLIGHT, context -> context.getRowIndex() == 0)
        );
        SpreadsheetComponentOptions<Object> options = new SpreadsheetComponentOptions<>(rules, provider);

        List<StyleCall> applied = new ArrayList<>();
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
                        applied.add(new StyleCall(row, column, style));
                    }

                    @Override
                    public void mergeCells(Object component, int firstRow, int lastRow, int firstColumn, int lastColumn) {
                    }

                    @Override
                    public void groupRows(Object component, int startRow, int endRow, boolean collapsed) {
                    }
                };

        DefaultSpreadsheetRenderer<Object, Object> renderer = new DefaultSpreadsheetRenderer<>(cellRenderer, options);
        renderer.render(new Object(), layout);

        Assertions.assertThat(applied).hasSize(2);
        StyleCall first = findStyle(applied, 0, 0);
        Assertions.assertThat(first.style).isEqualTo("base neg-style hl-style");
        StyleCall second = findStyle(applied, 1, 0);
        Assertions.assertThat(second.style).isEqualTo("neg-style");

        Assertions.assertThat(calls.getOrDefault(StyleToken.NEGATIVE, 0)).isEqualTo(1);
        Assertions.assertThat(calls.getOrDefault(StyleToken.HIGHLIGHT, 0)).isEqualTo(1);
    }

    private static StyleCall findStyle(List<StyleCall> applied, int row, int column) {
        return applied.stream()
                .filter(call -> call.row == row && call.column == column)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Style not applied for row=" + row + ", col=" + column));
    }

    private static final class StyleCall {
        private final int row;
        private final int column;
        private final String style;

        private StyleCall(int row, int column, String style) {
            this.row = row;
            this.column = column;
            this.style = style;
        }
    }

    private static final class TestLayout implements SpreadsheetLayout<Object> {
        private final List<CellBinding<Object>> bindings;

        private TestLayout(List<CellBinding<Object>> bindings) {
            this.bindings = bindings;
        }

        @Override
        public int getRowCount() {
            return 2;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public List<CellBinding<Object>> getCellBindings() {
            return bindings;
        }

        @Override
        public List<MergedRegion> getMergedRegions() {
            return List.of();
        }

        @Override
        public List<RowGroup> getRowGroups() {
            return List.of();
        }
    }
}
