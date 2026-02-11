package com.digtp.scm.portbalance.ui;

import com.digtp.scm.portbalance.aggregate.PortBalanceCell;
import com.digtp.scm.portbalance.aggregate.PortBalanceTable;
import com.digtp.scm.portbalance.layout.PortBalanceLayoutBuilder;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetController;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.index.DefaultLayoutIndex;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.internal.SpreadsheetInteractionBridge;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.render.DefaultSpreadsheetRenderer;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class PortBalanceSpreadsheetController implements SpreadsheetController<PortBalanceCell, Object> {

    private final Spreadsheet spreadsheet;
    private final Supplier<PortBalanceTable> tableSupplier;
    private final PortBalanceLayoutBuilder layoutBuilder;
    private final DefaultSpreadsheetRenderer<PortBalanceCell, Spreadsheet> renderer;
    private SpreadsheetInteractionHandler<PortBalanceCell> interactionHandler;
    private LayoutIndex<PortBalanceCell> layoutIndex;
    private SpreadsheetLayout<PortBalanceCell> currentLayout;
    private boolean bound;
    private boolean interactionBound;
    private boolean editListenerBound;
    private boolean readOnly = true;
    private boolean navigationGridVisible = true;
    private final Map<String, CellStyle> styleCache = new HashMap<>();
    private final Set<Cell> cellsToRefresh = new LinkedHashSet<>();

    public PortBalanceSpreadsheetController(Supplier<PortBalanceTable> tableSupplier,
                                            PortBalanceLayoutBuilder layoutBuilder) {
        if (tableSupplier == null) {
            throw new IllegalArgumentException("Table supplier cannot be null");
        }
        if (layoutBuilder == null) {
            throw new IllegalArgumentException("Layout builder cannot be null");
        }
        this.tableSupplier = tableSupplier;
        this.layoutBuilder = layoutBuilder;
        this.spreadsheet = new Spreadsheet();
        this.spreadsheet.setFunctionBarVisible(false);
        this.spreadsheet.setRowColHeadingsVisible(false);
        this.spreadsheet.setSheetSelectionBarVisible(false);
        this.renderer = new DefaultSpreadsheetRenderer<>(createCellRenderer());
    }

    @Override
    public void bind(SpreadsheetTableModel<PortBalanceCell> model, Object dataContainer) {
        if (bound) {
            throw new IllegalStateException("Controller is already bound");
        }
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        this.interactionHandler = model.getInteractionHandler();
        setupInteractionListener();
        setupEditListener();
        this.bound = true;
        reload();
    }

    @Override
    public void reload() {
        ensureBound();
        cellsToRefresh.clear();
        PortBalanceTable table = tableSupplier.get();
        currentLayout = layoutBuilder.buildLayout(table);
        resizeSheet(currentLayout);
        renderer.render(spreadsheet, currentLayout);
        refreshViewport(currentLayout);
        layoutIndex = new DefaultLayoutIndex<>(currentLayout, this::cellKey);
        applyLaycanColumnWidth(currentLayout);
        refreshGrouping(currentLayout);
        if (!cellsToRefresh.isEmpty()) {
            spreadsheet.refreshCells(cellsToRefresh);
        }
    }

    @Override
    public Spreadsheet getComponent() {
        ensureBound();
        return spreadsheet;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setNavigationGridVisible(boolean visible) {
        this.navigationGridVisible = visible;
        refreshGrouping(currentLayout);
    }

    @Override
    public boolean isNavigationGridVisible() {
        return navigationGridVisible;
    }

    @Override
    public void save() {
        ensureBound();
    }

    @Override
    public void updateAffectedCells(java.util.Set<Object> affectedEntityKeys) {
        reload();
    }

    private void ensureBound() {
        if (!bound) {
            throw new IllegalStateException("Controller is not bound");
        }
    }

    private Object cellKey(PortBalanceCell cell) {
        if (cell == null) {
            return null;
        }
        return cell.getRowId() + "|" + String.valueOf(cell.getColumnKey());
    }

    private void setupInteractionListener() {
        if (interactionBound || interactionHandler == null) {
            return;
        }
        spreadsheet.addSelectionChangeListener(event ->
                SpreadsheetInteractionBridge.handleSelectionChange(event, interactionHandler, layoutIndex));
        interactionBound = true;
    }

    private void setupEditListener() {
        if (editListenerBound) {
            return;
        }
        spreadsheet.addCellValueChangeListener(event -> {
            if (!readOnly) {
                return;
            }
            if (layoutIndex == null) {
                return;
            }
            var changedCells = event.getChangedCells();
            if (changedCells == null || changedCells.isEmpty()) {
                return;
            }
            for (org.apache.poi.ss.util.CellReference cellRef : changedCells) {
                int row = cellRef.getRow();
                int col = cellRef.getCol();
                revertCellValue(row, col);
            }
        });
        editListenerBound = true;
    }

    private void revertCellValue(int row, int col) {
        if (layoutIndex == null) {
            return;
        }
        LayoutIndex.CellRef cellRef = new CellRefImpl(row, col);
        var binding = layoutIndex.getCellBinding(cellRef);
        if (binding == null) {
            return;
        }
        Object value = binding.getValue();
        String formattedValue = value == null ? "" : value.toString();
        spreadsheet.createCell(row, col, formattedValue);
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
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LayoutIndex.CellRef ref)) {
                return false;
            }
            return row == ref.getRow() && column == ref.getColumn();
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, column);
        }
    }

    private DefaultSpreadsheetRenderer.CellRenderer<Spreadsheet> createCellRenderer() {
        return new DefaultSpreadsheetRenderer.CellRenderer<>() {
            @Override
            public void clear(Spreadsheet component) {
                try {
                    org.apache.poi.ss.usermodel.Workbook workbook = component.getWorkbook();
                    if (workbook != null && workbook.getNumberOfSheets() > 0) {
                        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                        if (sheet != null) {
                            resetRowGrouping(sheet);
                            int numMergedRegions = sheet.getNumMergedRegions();
                            for (int i = numMergedRegions - 1; i >= 0; i--) {
                                sheet.removeMergedRegion(i);
                            }

                            java.util.Iterator<org.apache.poi.ss.usermodel.Row> rowIterator = sheet.rowIterator();
                            while (rowIterator.hasNext()) {
                                org.apache.poi.ss.usermodel.Row row = rowIterator.next();
                                java.util.Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row.cellIterator();
                                while (cellIterator.hasNext()) {
                                    org.apache.poi.ss.usermodel.Cell cell = cellIterator.next();
                                    cell.setBlank();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    java.util.logging.Logger.getLogger(PortBalanceSpreadsheetController.class.getName())
                            .warning("Failed to clear spreadsheet: " + e.getMessage());
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
                    org.apache.poi.ss.usermodel.Workbook workbook = component.getWorkbook();
                    if (workbook == null || workbook.getNumberOfSheets() == 0) {
                        return;
                    }
                    org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                    sheet.groupRow(startRow, endRow);
                    sheet.setRowGroupCollapsed(startRow, collapsed);
                } catch (Exception e) {
                    java.util.logging.Logger.getLogger(PortBalanceSpreadsheetController.class.getName())
                            .warning("Failed to group rows: " + e.getMessage());
                }
            }
        };
    }

    private void refreshGrouping(SpreadsheetLayout<PortBalanceCell> layout) {
        boolean hasGroups = layout != null && !layout.getRowGroups().isEmpty();
        spreadsheet.setRowColHeadingsVisible(navigationGridVisible && hasGroups);
        if (!hasGroups) {
            return;
        }
        try {
            Class<?> factory = Class.forName("com.vaadin.flow.component.spreadsheet.SpreadsheetFactory");
            java.lang.reflect.Method loadGrouping =
                    factory.getDeclaredMethod("loadGrouping", Spreadsheet.class);
            loadGrouping.setAccessible(true);
            loadGrouping.invoke(null, spreadsheet);
        } catch (ReflectiveOperationException e) {
            java.util.logging.Logger.getLogger(PortBalanceSpreadsheetController.class.getName())
                    .warning("Failed to refresh row grouping: " + e.getMessage());
        }
    }

    private void resizeSheet(SpreadsheetLayout<PortBalanceCell> layout) {
        if (layout == null) {
            return;
        }
        int rows = Math.max(1, layout.getRowCount());
        int columns = Math.max(1, layout.getColumnCount());
        spreadsheet.setSheetMaxSize(rows, columns);
    }

    private void applyLaycanColumnWidth(SpreadsheetLayout<PortBalanceCell> layout) {
        if (layout == null) {
            return;
        }
        Integer laycanColumn = findHeaderColumn(layout, "Laycan");
        if (laycanColumn == null) {
            return;
        }
        int width = spreadsheet.getDefaultColumnWidth() + 100;
        spreadsheet.setColumnWidth(laycanColumn, width);
    }

    private void refreshViewport(SpreadsheetLayout<PortBalanceCell> layout) {
        ensureViewportRange(layout);
        try {
            spreadsheet.refreshAllCellValues();
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(PortBalanceSpreadsheetController.class.getName())
                    .warning("Failed to refresh spreadsheet viewport: " + e.getMessage());
        }
    }

    private void ensureViewportRange(SpreadsheetLayout<PortBalanceCell> layout) {
        if (layout == null) {
            return;
        }
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
            setViewportField("firstColumn", 1);
            setViewportField("lastColumn", Math.max(1, expectedColumns));
            setViewportField("firstRow", 1);
            setViewportField("lastRow", Math.max(1, expectedRows));
        } catch (ReflectiveOperationException e) {
            java.util.logging.Logger.getLogger(PortBalanceSpreadsheetController.class.getName())
                    .warning("Failed to initialize spreadsheet viewport: " + e.getMessage());
        }
    }

    private void setViewportField(String fieldName, int value) throws ReflectiveOperationException {
        java.lang.reflect.Field field = Spreadsheet.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(spreadsheet, value);
    }

    private Integer findHeaderColumn(SpreadsheetLayout<PortBalanceCell> layout, String header) {
        for (CellBinding<PortBalanceCell> binding : layout.getCellBindings()) {
            if (binding.getRowIndex() != 0) {
                continue;
            }
            Object value = binding.getValue();
            if (header.equals(value)) {
                return binding.getColumnIndex();
            }
        }
        return null;
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
            java.lang.reflect.Method getCtRow = row.getClass().getMethod("getCTRow");
            Object ctRow = getCtRow.invoke(row);
            if (ctRow == null) {
                return;
            }
            java.lang.reflect.Method outlineSetter = null;
            for (java.lang.reflect.Method method : ctRow.getClass().getMethods()) {
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
            // Outline level reset is best-effort; ignore if POI internals change.
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
