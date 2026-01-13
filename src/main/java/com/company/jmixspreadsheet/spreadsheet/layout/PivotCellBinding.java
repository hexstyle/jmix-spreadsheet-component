package com.company.jmixspreadsheet.spreadsheet.layout;

import java.util.List;

/**
 * Cell binding implementation for pivot table cells.
 * <p>
 * This binding represents a pivot cell that aggregates multiple source entities.
 * It includes a pivot context with the source entities that contribute to the cell value.
 *
 * @param <E> the entity type
 */
public class PivotCellBinding<E> implements CellBinding<E> {

    private final int rowIndex;
    private final int columnIndex;
    private final Object value;
    private final String style;
    private final PivotContext pivotContext;
    private final List<E> sourceEntities;

    /**
     * Creates a new pivot cell binding.
     *
     * @param rowIndex the row index (0-based)
     * @param columnIndex the column index (0-based)
     * @param value the aggregated value
     * @param style the cell style, or {@code null} for default
     * @param sourceEntities the source entities that contribute to this cell
     */
    public PivotCellBinding(
            int rowIndex,
            int columnIndex,
            Object value,
            String style,
            List<E> sourceEntities) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.value = value;
        this.style = style;
        this.sourceEntities = sourceEntities != null 
                ? java.util.Collections.unmodifiableList(sourceEntities) 
                : java.util.Collections.emptyList();
        this.pivotContext = new DefaultPivotContext();
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
    public Object getValue() {
        return value;
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public E getEntityRef() {
        // Pivot cells don't have a single entity reference
        return null;
    }

    @Override
    public PivotContext getPivotContext() {
        return pivotContext;
    }

    /**
     * Returns the source entities that contribute to this pivot cell.
     *
     * @return the list of source entities
     */
    public List<E> getSourceEntities() {
        return sourceEntities;
    }

    /**
     * Default implementation of PivotContext.
     */
    private static class DefaultPivotContext implements PivotContext {
        DefaultPivotContext() {
            // Marker interface implementation
        }
    }
}
