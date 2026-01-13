package com.company.jmixspreadsheet.spreadsheet.render;

import com.company.jmixspreadsheet.spreadsheet.layout.CellBinding;
import com.company.jmixspreadsheet.spreadsheet.layout.MergedRegion;
import com.company.jmixspreadsheet.spreadsheet.layout.SpreadsheetLayout;

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

    /**
     * Creates a new renderer with the given cell renderer strategy.
     *
     * @param cellRenderer the strategy for rendering cells to the component
     */
    public DefaultSpreadsheetRenderer(CellRenderer<C> cellRenderer) {
        if (cellRenderer == null) {
            throw new IllegalArgumentException("Cell renderer cannot be null");
        }
        this.cellRenderer = cellRenderer;
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

            // Apply cell style if specified
            String style = binding.getStyle();
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
    }
}
