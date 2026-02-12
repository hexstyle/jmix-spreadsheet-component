package com.hexstyle.jmixspreadsheet.render;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Sheet;

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
        try {
            recalculateSheetSizes(spreadsheet);
            refreshMergedRegions(spreadsheet);
            if (hasVisibleRange(spreadsheet)) {
                spreadsheet.reloadVisibleCellContents();
            }
            spreadsheet.refreshAllCellValues();
        } catch (Exception e) {
            logger.warning("Failed to refresh spreadsheet viewport: " + e.getMessage());
        }
    }

    public static void refreshViewportAfterLayout(Spreadsheet spreadsheet, SpreadsheetLayout<?> layout) {
        if (spreadsheet == null || layout == null) {
            return;
        }
        spreadsheet.getUI().ifPresentOrElse(
                ui -> ui.beforeClientResponse(spreadsheet, context -> refreshViewport(spreadsheet, layout)),
                () -> refreshViewport(spreadsheet, layout)
        );
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

    private static boolean hasVisibleRange(Spreadsheet spreadsheet) {
        int firstColumn = spreadsheet.getFirstColumn();
        int lastColumn = spreadsheet.getLastColumn();
        int firstRow = spreadsheet.getFirstRow();
        int lastRow = spreadsheet.getLastRow();
        return firstColumn > 0
                && firstRow > 0
                && lastColumn >= firstColumn
                && lastRow >= firstRow;
    }

    private static void refreshMergedRegions(Spreadsheet spreadsheet) {
        try {
            spreadsheet.reloadAllMergedRegions();
        } catch (Exception e) {
            logger.warning("Failed to refresh spreadsheet merged regions: " + e.getMessage());
        }
    }

    private static void recalculateSheetSizes(Spreadsheet spreadsheet) {
        try {
            Class<?> factory = Class.forName("com.vaadin.flow.component.spreadsheet.SpreadsheetFactory");
            Method calculateSheetSizes = factory.getDeclaredMethod(
                    "calculateSheetSizes",
                    Spreadsheet.class,
                    Sheet.class
            );
            calculateSheetSizes.setAccessible(true);
            calculateSheetSizes.invoke(null, spreadsheet, spreadsheet.getActiveSheet());
        } catch (ReflectiveOperationException e) {
            logger.warning("Failed to recalculate spreadsheet sheet sizes: " + e.getMessage());
        }
    }

}
