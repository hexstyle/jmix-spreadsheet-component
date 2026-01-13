package com.company.jmixspreadsheet.spreadsheet.layout;

/**
 * Default implementation of {@link CellBinding} for flat table cells.
 * <p>
 * This binding represents a regular cell that displays a single entity's property value.
 *
 * @param <E> the entity type
 */
public class DefaultCellBinding<E> implements CellBinding<E> {

    private final int rowIndex;
    private final int columnIndex;
    private final Object value;
    private final String style;
    private final E entityRef;

    /**
     * Creates a new cell binding.
     *
     * @param rowIndex the row index (0-based)
     * @param columnIndex the column index (0-based)
     * @param value the cell value
     * @param style the cell style, or {@code null} for default
     * @param entityRef the entity reference, or {@code null} for header cells
     */
    public DefaultCellBinding(
            int rowIndex,
            int columnIndex,
            Object value,
            String style,
            E entityRef) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.value = value;
        this.style = style;
        this.entityRef = entityRef;
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
        return entityRef;
    }

    @Override
    public PivotContext getPivotContext() {
        // Regular cells don't have pivot context
        return null;
    }
}
