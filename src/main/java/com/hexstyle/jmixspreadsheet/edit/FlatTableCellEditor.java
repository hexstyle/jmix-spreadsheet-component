package com.hexstyle.jmixspreadsheet.edit;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex.CellRef;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;

/**
 * Handles cell editing for flat tables.
 * <p>
 * This editor processes cell edit events and updates entities using column setters.
 * It only supports flat tables - no pivot editing.
 * <p>
 * The editing flow:
 * 1. User edits cell
 * 2. CellBinding → (entity, setter) via LayoutIndex
 * 3. Update entity property
 * 4. Entity is saved via DataManager
 *
 * @param <E> the entity type
 */
public class FlatTableCellEditor<E> {

    private final SpreadsheetTableModel<E> model;
    private final LayoutIndex<E> layoutIndex;
    private final EntityAdapter<E> entityAdapter;

    /**
     * Creates a new cell editor.
     *
     * @param model the table model defining columns
     * @param layoutIndex the layout index for mapping cells to entities
     * @param entityAdapter adapter for entity operations
     */
    public FlatTableCellEditor(
            SpreadsheetTableModel<E> model,
            LayoutIndex<E> layoutIndex,
            EntityAdapter<E> entityAdapter) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (layoutIndex == null) {
            throw new IllegalArgumentException("Layout index cannot be null");
        }
        if (entityAdapter == null) {
            throw new IllegalArgumentException("Entity adapter cannot be null");
        }

        this.model = model;
        this.layoutIndex = layoutIndex;
        this.entityAdapter = entityAdapter;
    }

    /**
     * Handles a cell edit event.
     * <p>
     * This method updates the entity associated with the edited cell using
     * the column's setter and DataContext. The DataContainer will automatically
     * emit change events after the entity is updated.
     *
     * @param rowIndex the row index of the edited cell (0-based)
     * @param columnIndex the column index of the edited cell (0-based)
     * @param newValue the new cell value
     * @throws IllegalArgumentException if the cell is not editable or not found
     * @throws IllegalStateException if the cell does not have an entity binding
     */
    public void handleCellEdit(int rowIndex, int columnIndex, Object newValue) {
        // Create cell reference
        CellRef cellRef = createCellRef(rowIndex, columnIndex);

        // Get cell binding from layout index
        CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
        if (binding == null) {
            throw new IllegalArgumentException("Cell not found in layout index: row=" + rowIndex + ", column=" + columnIndex);
        }

        // Check if this is a flat table cell (not a pivot cell)
        E entity = binding.getEntityRef();
        if (entity == null) {
            throw new IllegalStateException("Cell is not bound to an entity (may be a pivot cell or header): row=" + rowIndex + ", column=" + columnIndex);
        }

        // Get column for this cell
        SpreadsheetColumn<E> column = getColumnByIndex(columnIndex);
        if (column == null) {
            throw new IllegalArgumentException("Column not found at index: " + columnIndex);
        }

        // Check if column is editable
        if (!column.isEditable()) {
            throw new IllegalArgumentException("Column is not editable: " + column.getId());
        }

        // Check if column has a setter
        var setter = column.getSetter();
        if (setter == null) {
            throw new IllegalArgumentException("Column does not have a setter: " + column.getId());
        }

        // With DataManager, we don't need to merge - just update the entity directly
        // The entity will be saved via DataManager.saveAll()
        setter.accept(entity, newValue);
    }

    /**
     * Creates a cell reference for the given row and column indices.
     *
     * @param row the row index (0-based)
     * @param column the column index (0-based)
     * @return the cell reference
     */
    private CellRef createCellRef(int row, int column) {
        return new CellRef() {
            @Override
            public int getRow() {
                return row;
            }

            @Override
            public int getColumn() {
                return column;
            }
        };
    }

    /**
     * Gets the column at the given index.
     *
     * @param columnIndex the column index (0-based)
     * @return the column, or {@code null} if index is out of bounds
     */
    private SpreadsheetColumn<E> getColumnByIndex(int columnIndex) {
        var columns = model.getColumns();
        if (columnIndex < 0 || columnIndex >= columns.size()) {
            return null;
        }
        return columns.get(columnIndex);
    }

    /**
     * Adapter interface for entity operations.
     * <p>
     * This abstraction allows the cell editor to work with entities.
     * With DataManager, merge is not needed - entities are saved directly.
     *
     * @param <E> the entity type
     */
    public interface EntityAdapter<E> {
        /**
         * Processes an entity (no-op with DataManager, kept for interface compatibility).
         *
         * @param entity the entity
         * @return the entity (unchanged)
         */
        E merge(E entity);
    }
}
