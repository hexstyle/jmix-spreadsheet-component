package com.hexstyle.jmixspreadsheet.render;

import com.hexstyle.jmixspreadsheet.ui.SpreadsheetComponentOptions;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Spreadsheet renderer that applies inline CSS-like styles to Vaadin Spreadsheet (POI).
 *
 * @param <E> the entity type
 */
public class PoiSpreadsheetRenderer<E> extends DefaultSpreadsheetRenderer<E, Spreadsheet> {

    private final PoiCellRenderer cellRenderer;

    public PoiSpreadsheetRenderer(SpreadsheetComponentOptions<E> options) {
        this(new PoiCellRenderer(), options);
    }

    public PoiSpreadsheetRenderer(PoiCellRenderer cellRenderer, SpreadsheetComponentOptions<E> options) {
        super(cellRenderer, options);
        this.cellRenderer = cellRenderer;
    }

    public void flush(Spreadsheet spreadsheet) {
        cellRenderer.flush(spreadsheet);
    }

    public void clearUpdates() {
        cellRenderer.clearUpdates();
    }

    /**
     * Cell renderer that maps simple CSS-like styles to POI styles.
     */
    public static class PoiCellRenderer implements CellRenderer<Spreadsheet> {

        private static final Logger logger = Logger.getLogger(PoiCellRenderer.class.getName());
        private final Map<String, CellStyle> styleCache = new HashMap<>();
        private final Set<Cell> cellsToRefresh = new LinkedHashSet<>();

        @Override
        public void clear(Spreadsheet component) {
            clearUpdates();
            try {
                Workbook workbook = component.getWorkbook();
                if (workbook != null && workbook.getNumberOfSheets() > 0) {
                    var sheet = workbook.getSheetAt(0);
                    if (sheet != null) {
                        resetRowGrouping(sheet);
                        int numMergedRegions = sheet.getNumMergedRegions();
                        for (int i = numMergedRegions - 1; i >= 0; i--) {
                            sheet.removeMergedRegion(i);
                        }
                        // Keep Spreadsheet state in sync with POI after bulk merge cleanup.
                        component.reloadAllMergedRegions();
                        var rowIterator = sheet.rowIterator();
                        while (rowIterator.hasNext()) {
                            var row = rowIterator.next();
                            var cellIterator = row.cellIterator();
                            while (cellIterator.hasNext()) {
                                var cell = cellIterator.next();
                                cell.setBlank();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warning("Failed to clear spreadsheet: " + e.getMessage());
            }
        }

        @Override
        public void setCellValue(Spreadsheet component, int row, int column, String value) {
            Cell cell = component.createCell(row, column, value);
            if (cell != null) {
                cellsToRefresh.add(cell);
            }
        }

        @Override
        public void setCellStyle(Spreadsheet component, int row, int column, String style) {
            if (style == null || style.isBlank()) {
                return;
            }
            Workbook workbook = component.getWorkbook();
            if (workbook == null) {
                return;
            }
            Cell cell = component.getCell(row, column);
            if (cell == null) {
                cell = component.createCell(row, column, "");
            }
            CellStyle cellStyle = styleCache.computeIfAbsent(style, key -> buildCellStyle(workbook, key));
            cell.setCellStyle(cellStyle);
            cellsToRefresh.add(cell);
            component.getSpreadsheetStyleFactory().cellStyleUpdated(cell, true);
        }

        @Override
        public void mergeCells(Spreadsheet component, int firstRow, int lastRow,
                               int firstColumn, int lastColumn) {
            component.addMergedRegion(firstRow, firstColumn, lastRow, lastColumn);
        }

        @Override
        public void groupRows(Spreadsheet component, int startRow, int endRow, boolean collapsed) {
            try {
                Workbook workbook = component.getWorkbook();
                if (workbook == null || workbook.getNumberOfSheets() == 0) {
                    return;
                }
                var sheet = workbook.getSheetAt(0);
                sheet.groupRow(startRow, endRow);
                sheet.setRowGroupCollapsed(startRow, collapsed);
            } catch (Exception e) {
                logger.warning("Failed to group rows: " + e.getMessage());
            }
        }

        void flush(Spreadsheet component) {
            if (!cellsToRefresh.isEmpty()) {
                component.refreshCells(cellsToRefresh);
                cellsToRefresh.clear();
            }
        }

        void clearUpdates() {
            cellsToRefresh.clear();
        }

        private CellStyle buildCellStyle(Workbook workbook, String style) {
            CellStyle cellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            boolean fontModified = false;

            StyleAttributes attributes = parseStyle(style);
            if (attributes.background != null) {
                applyFillColor(workbook, cellStyle, attributes.background);
            }
            if (attributes.fontColor != null) {
                applyFontColor(workbook, font, attributes.fontColor);
                fontModified = true;
            }
            if (attributes.bold) {
                font.setBold(true);
                fontModified = true;
            }
            if (attributes.borderTop != null) {
                cellStyle.setBorderTop(attributes.borderTop);
                applyBorderColor(workbook, cellStyle, attributes.borderTopColor, true);
            }
            if (attributes.borderBottom != null) {
                cellStyle.setBorderBottom(attributes.borderBottom);
                applyBorderColor(workbook, cellStyle, attributes.borderBottomColor, false);
            }
            if (fontModified) {
                cellStyle.setFont(font);
            }
            return cellStyle;
        }

        private void applyFillColor(Workbook workbook, CellStyle cellStyle, Color color) {
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            if (workbook instanceof XSSFWorkbook && cellStyle instanceof XSSFCellStyle xssfStyle) {
                xssfStyle.setFillForegroundColor(new XSSFColor(color, null));
            }
        }

        private void applyFontColor(Workbook workbook, Font font, Color color) {
            if (workbook instanceof XSSFWorkbook && font instanceof XSSFFont xssfFont) {
                xssfFont.setColor(new XSSFColor(color, null));
            }
        }

        private void applyBorderColor(Workbook workbook,
                                      CellStyle cellStyle,
                                      Color color,
                                      boolean top) {
            if (color == null) {
                return;
            }
            if (workbook instanceof XSSFWorkbook && cellStyle instanceof XSSFCellStyle xssfStyle) {
                if (top) {
                    xssfStyle.setTopBorderColor(new XSSFColor(color, null));
                } else {
                    xssfStyle.setBottomBorderColor(new XSSFColor(color, null));
                }
            }
        }

        private StyleAttributes parseStyle(String style) {
            StyleAttributes attributes = new StyleAttributes();
            String[] entries = style.split(";");
            for (String entry : entries) {
                String trimmed = entry.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                int colon = trimmed.indexOf(':');
                if (colon <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, colon).trim();
                String value = trimmed.substring(colon + 1).trim();
                switch (key) {
                    case "background-color" -> attributes.background = parseColor(value);
                    case "color" -> attributes.fontColor = parseColor(value);
                    case "font-weight" -> attributes.bold = isBold(value);
                    case "border-top" -> applyBorder(attributes, value, true);
                    case "border-bottom" -> applyBorder(attributes, value, false);
                    default -> {
                    }
                }
            }
            return attributes;
        }

        private void applyBorder(StyleAttributes attributes, String value, boolean top) {
            BorderStyle borderStyle = BorderStyle.MEDIUM;
            Color color = null;
            String[] parts = value.split("\\s+");
            for (String part : parts) {
                if (part.startsWith("#")) {
                    color = parseColor(part);
                }
            }
            if (top) {
                attributes.borderTop = borderStyle;
                attributes.borderTopColor = color;
            } else {
                attributes.borderBottom = borderStyle;
                attributes.borderBottomColor = color;
            }
        }

        private boolean isBold(String value) {
            if ("bold".equalsIgnoreCase(value)) {
                return true;
            }
            try {
                int weight = Integer.parseInt(value);
                return weight >= 600;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        private Color parseColor(String value) {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            if (hex.length() != 6) {
                return null;
            }
            try {
                int rgb = Integer.parseInt(hex, 16);
                return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        private void resetRowGrouping(org.apache.poi.ss.usermodel.Sheet sheet) {
            if (sheet == null) {
                return;
            }
            int first = sheet.getFirstRowNum();
            int last = sheet.getLastRowNum();
            if (first < 0 || last < 0) {
                return;
            }
            for (int i = first; i <= last; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                row.setZeroHeight(false);
                resetOutlineLevel(row);
            }
        }

        private void resetOutlineLevel(org.apache.poi.ss.usermodel.Row row) {
            try {
                Method getCtRow = row.getClass().getMethod("getCTRow");
                Object ctRow = getCtRow.invoke(row);
                if (ctRow == null) {
                    return;
                }
                Method outlineSetter = null;
                for (Method method : ctRow.getClass().getMethods()) {
                    if ("setOutlineLevel".equals(method.getName()) && method.getParameterCount() == 1) {
                        outlineSetter = method;
                        break;
                    }
                }
                if (outlineSetter == null) {
                    return;
                }
                Class<?> paramType = outlineSetter.getParameterTypes()[0];
                Object value = (paramType == short.class || paramType == Short.class) ? (short) 0 : 0;
                outlineSetter.invoke(ctRow, value);
            } catch (ReflectiveOperationException ignored) {
                // Best-effort cleanup.
            }
        }

        private static class StyleAttributes {
            private Color background;
            private Color fontColor;
            private boolean bold;
            private BorderStyle borderTop;
            private BorderStyle borderBottom;
            private Color borderTopColor;
            private Color borderBottomColor;
        }
    }
}
