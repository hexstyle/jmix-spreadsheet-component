package com.hexstyle.jmixspreadsheet.render;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.ui.CellStyleContext;
import com.hexstyle.jmixspreadsheet.ui.SpreadsheetComponentOptions;
import com.hexstyle.jmixspreadsheet.ui.StyleRule;
import com.hexstyle.jmixspreadsheet.ui.StyleToken;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Default implementation of {@link SpreadsheetRenderer}.
 * <p>
 * This renderer performs the initial render of a spreadsheet layout by setting
 * cell values, applying styles, and merging regions. It does not perform any
 * diff logic or incremental updates - only full initial rendering.
 *
 * @param <E> the entity type
 * @param <C> the component type
 */
public class DefaultSpreadsheetRenderer<E, C> implements SpreadsheetRenderer<E, C> {

    private final CellRenderer<C> cellRenderer;
    private final SpreadsheetComponentOptions<E> options;
    private final EnumMap<StyleToken, String> styleCache = new EnumMap<>(StyleToken.class);

    /**
     * Creates a new renderer with the given cell renderer strategy.
     *
     * @param cellRenderer the strategy for rendering cells to the component
     */
    public DefaultSpreadsheetRenderer(CellRenderer<C> cellRenderer) {
        this(cellRenderer, SpreadsheetComponentOptions.<E>empty());
    }

    /**
     * Creates a new renderer with the given cell renderer strategy and options.
     *
     * @param cellRenderer the strategy for rendering cells to the component
     * @param options rendering options including conditional styling rules
     */
    public DefaultSpreadsheetRenderer(CellRenderer<C> cellRenderer, SpreadsheetComponentOptions<E> options) {
        if (cellRenderer == null) {
            throw new IllegalArgumentException("Cell renderer cannot be null");
        }
        this.cellRenderer = cellRenderer;
        this.options = options == null ? SpreadsheetComponentOptions.<E>empty() : options;
    }

    @Override
    public void render(C component, SpreadsheetLayout<E> layout) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }
        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null");
        }

        // Clear existing content (if needed by the component)
        cellRenderer.clear(component);

        // Render merged regions first (before cell values, so merging doesn't overwrite values)
        for (MergedRegion mergedRegion : layout.getMergedRegions()) {
            cellRenderer.mergeCells(
                    component,
                    mergedRegion.getFirstRow(),
                    mergedRegion.getLastRow(),
                    mergedRegion.getFirstColumn(),
                    mergedRegion.getLastColumn()
            );
        }

        // Apply row grouping before setting values, so outline metadata is in place
        for (com.hexstyle.jmixspreadsheet.layout.RowGroup rowGroup : layout.getRowGroups()) {
            cellRenderer.groupRows(
                    component,
                    rowGroup.getStartRow(),
                    rowGroup.getEndRow(),
                    rowGroup.isCollapsed()
            );
        }

        // Render all cell bindings
        for (CellBinding<E> binding : layout.getCellBindings()) {
            Object value = binding.getValue();
            String formattedValue = formatValue(value, binding);
            
            cellRenderer.setCellValue(
                    component,
                    binding.getRowIndex(),
                    binding.getColumnIndex(),
                    formattedValue
            );

            // Apply cell style if specified or resolved from rules
            String style = resolveStyle(binding, layout);
            if (style != null) {
                cellRenderer.setCellStyle(
                        component,
                        binding.getRowIndex(),
                        binding.getColumnIndex(),
                        style
                );
            }
        }
    }

    /**
     * Formats a cell value for display.
     *
     * @param value the raw value
     * @param binding the cell binding (may contain formatting information)
     * @return the formatted string value
     */
    private String formatValue(Object value, CellBinding<E> binding) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private String resolveStyle(CellBinding<E> binding, SpreadsheetLayout<E> layout) {
        String baseStyle = binding.getStyle();
        List<StyleToken> tokens = resolveTokens(binding, layout);
        return combineStyles(baseStyle, tokens);
    }

    private List<StyleToken> resolveTokens(CellBinding<E> binding, SpreadsheetLayout<E> layout) {
        List<StyleRule<E>> rules = options.getStyleRules();
        if (rules.isEmpty()) {
            return List.of();
        }
        CellStyleContext<E> context = new CellStyleContext<>(layout, binding);
        List<StyleToken> tokens = new ArrayList<>();
        for (StyleRule<E> rule : rules) {
            if (rule.matches(context)) {
                tokens.add(rule.getToken());
            }
        }
        return tokens;
    }

    private String combineStyles(String baseStyle, List<StyleToken> tokens) {
        StringBuilder combined = new StringBuilder();
        appendStyle(combined, baseStyle);
        for (StyleToken token : tokens) {
            appendStyle(combined, getStyleForToken(token));
        }
        if (combined.length() == 0) {
            return null;
        }
        return combined.toString();
    }

    private String getStyleForToken(StyleToken token) {
        if (!styleCache.containsKey(token)) {
            styleCache.put(token, options.getStyleProvider().apply(token));
        }
        return styleCache.get(token);
    }

    private void appendStyle(StringBuilder combined, String style) {
        if (style == null) {
            return;
        }
        String trimmed = style.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (combined.length() > 0) {
            combined.append(' ');
        }
        combined.append(trimmed);
    }

    /**
     * Strategy interface for rendering cells to a spreadsheet component.
     * <p>
     * This abstraction allows the renderer to work with different component types
     * without direct dependencies on Vaadin or other UI frameworks.
     *
     * @param <C> the component type
     */
    public interface CellRenderer<C> {
        /**
         * Clears the content of the spreadsheet component.
         *
         * @param component the spreadsheet component
         */
        void clear(C component);

        /**
         * Sets the value of a cell in the spreadsheet.
         *
         * @param component the spreadsheet component
         * @param row the row index (0-based)
         * @param column the column index (0-based)
         * @param value the value to set (as a string)
         */
        void setCellValue(C component, int row, int column, String value);

        /**
         * Sets the style of a cell in the spreadsheet.
         *
         * @param component the spreadsheet component
         * @param row the row index (0-based)
         * @param column the column index (0-based)
         * @param style the style to apply
         */
        void setCellStyle(C component, int row, int column, String style);

        /**
         * Merges cells in the specified region.
         *
         * @param component the spreadsheet component
         * @param firstRow the first row index (0-based)
         * @param lastRow the last row index (0-based, inclusive)
         * @param firstColumn the first column index (0-based)
         * @param lastColumn the last column index (0-based, inclusive)
         */
        void mergeCells(C component, int firstRow, int lastRow, int firstColumn, int lastColumn);

        /**
         * Groups rows for outline/expandable sections.
         *
         * @param component the spreadsheet component
         * @param startRow the first row index (0-based)
         * @param endRow the last row index (0-based, inclusive)
         * @param collapsed whether the group is collapsed
         */
        void groupRows(C component, int startRow, int endRow, boolean collapsed);
    }
}
