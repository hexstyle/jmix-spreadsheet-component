package com.hexstyle.jmixspreadsheet.render;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Utilities for keeping Vaadin Spreadsheet rendering in sync with layout changes.
 */
public final class SpreadsheetRenderSupport {

    private static final Logger logger = Logger.getLogger(SpreadsheetRenderSupport.class.getName());

    private SpreadsheetRenderSupport() {
    }

    public static void resizeSheet(Spreadsheet spreadsheet, SpreadsheetLayout<?> layout) {
        if (spreadsheet == null || layout == null) {
            return;
        }
        int rows = Math.max(1, layout.getRowCount());
        int columns = Math.max(1, layout.getColumnCount());
        spreadsheet.setSheetMaxSize(rows, columns);
    }

    public static void refreshViewport(Spreadsheet spreadsheet, SpreadsheetLayout<?> layout) {
        if (spreadsheet == null || layout == null) {
            return;
        }
        ensureViewportRange(spreadsheet, layout);
        try {
            spreadsheet.refreshAllCellValues();
        } catch (Exception e) {
            logger.warning("Failed to refresh spreadsheet viewport: " + e.getMessage());
        }
    }

    public static void refreshGrouping(Spreadsheet spreadsheet,
                                       SpreadsheetLayout<?> layout,
                                       boolean navigationGridVisible) {
        if (spreadsheet == null) {
            return;
        }
        boolean hasGroups = layout != null && !layout.getRowGroups().isEmpty();
        spreadsheet.setRowColHeadingsVisible(navigationGridVisible);
        if (!hasGroups) {
            return;
        }
        try {
            Class<?> factory = Class.forName("com.vaadin.flow.component.spreadsheet.SpreadsheetFactory");
            Method loadGrouping = factory.getDeclaredMethod("loadGrouping", Spreadsheet.class);
            loadGrouping.setAccessible(true);
            loadGrouping.invoke(null, spreadsheet);
        } catch (ReflectiveOperationException e) {
            logger.warning("Failed to refresh row grouping: " + e.getMessage());
        }
    }

    public static void applyHeaderColumnWidths(Spreadsheet spreadsheet,
                                               SpreadsheetLayout<?> layout,
                                               int headerRowIndex,
                                               Map<String, Integer> widthOverrides,
                                               Map<String, Integer> widthDeltas) {
        if (spreadsheet == null || layout == null) {
            return;
        }
        if (widthOverrides != null) {
            widthOverrides.forEach((header, width) -> {
                Integer column = findHeaderColumn(layout, header, headerRowIndex);
                if (column == null || width == null) {
                    return;
                }
                spreadsheet.setColumnWidth(column, width);
            });
        }
        if (widthDeltas != null) {
            int baseWidth = spreadsheet.getDefaultColumnWidth();
            widthDeltas.forEach((header, delta) -> {
                Integer column = findHeaderColumn(layout, header, headerRowIndex);
                if (column == null || delta == null) {
                    return;
                }
                spreadsheet.setColumnWidth(column, Math.max(1, baseWidth + delta));
            });
        }
    }

    private static Integer findHeaderColumn(SpreadsheetLayout<?> layout, String header, int headerRowIndex) {
        if (layout == null || header == null) {
            return null;
        }
        for (CellBinding<?> binding : layout.getCellBindings()) {
            if (binding.getRowIndex() != headerRowIndex) {
                continue;
            }
            Object value = binding.getValue();
            if (Objects.equals(header, value)) {
                return binding.getColumnIndex();
            }
        }
        return null;
    }

    private static void ensureViewportRange(Spreadsheet spreadsheet, SpreadsheetLayout<?> layout) {
        int expectedColumns = layout.getColumnCount();
        int expectedRows = layout.getRowCount();
        if (expectedColumns <= 1 && expectedRows <= 1) {
            return;
        }
        int firstColumn = spreadsheet.getFirstColumn();
        int lastColumn = spreadsheet.getLastColumn();
        int firstRow = spreadsheet.getFirstRow();
        int lastRow = spreadsheet.getLastRow();
        if (firstColumn > 0 && lastColumn > 1 && firstRow > 0 && lastRow > 1) {
            return;
        }
        try {
            setViewportField(spreadsheet, "firstColumn", 1);
            setViewportField(spreadsheet, "lastColumn", Math.max(1, expectedColumns));
            setViewportField(spreadsheet, "firstRow", 1);
            setViewportField(spreadsheet, "lastRow", Math.max(1, expectedRows));
        } catch (ReflectiveOperationException e) {
            logger.warning("Failed to initialize spreadsheet viewport: " + e.getMessage());
        }
    }

    private static void setViewportField(Spreadsheet spreadsheet, String fieldName, int value)
            throws ReflectiveOperationException {
        Field field = Spreadsheet.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(spreadsheet, value);
    }
}
