package com.hexstyle.jmixspreadsheet.index;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.PivotCellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;

import java.util.*;

/**
 * Default implementation of {@link LayoutIndex}.
 * <p>
 * This implementation builds and maintains mappings between entity keys and cell references,
 * as well as between cell references and cell bindings. It also tracks pivot cells and
 * their contributing entity keys.
 *
 * @param <E> the entity type
 */
public class DefaultLayoutIndex<E> implements LayoutIndex<E> {

    // entityKey → Set<CellRef>
    private final Map<Object, Set<CellRef>> entityKeyToCellRefs = new HashMap<>();

    // cellRef → CellBinding
    private final Map<CellRef, CellBinding<E>> cellRefToBinding = new HashMap<>();

    // pivotCell → Set<entityKey>
    private final Map<CellRef, Set<Object>> pivotCellToEntityKeys = new HashMap<>();

    /**
     * Creates a new layout index from a spreadsheet layout.
     * <p>
     * The index is built by iterating through all cell bindings in the layout
     * and establishing the mappings between entities, cells, and bindings.
     *
     * @param layout the spreadsheet layout to index
     * @param entityKeyProvider function that extracts entity keys from entities
     */
    public DefaultLayoutIndex(SpreadsheetLayout<E> layout, java.util.function.Function<E, Object> entityKeyProvider) {
        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null");
        }
        if (entityKeyProvider == null) {
            throw new IllegalArgumentException("Entity key provider cannot be null");
        }

        buildIndex(layout, entityKeyProvider);
    }

    @Override
    public Set<CellRef> getCellRefs(Object entityKey) {
        return entityKeyToCellRefs.getOrDefault(entityKey, Collections.emptySet());
    }

    @Override
    public CellBinding<E> getCellBinding(CellRef cellRef) {
        return cellRefToBinding.get(cellRef);
    }

    @Override
    public Set<Object> getEntityKeys(CellRef cellRef) {
        return pivotCellToEntityKeys.getOrDefault(cellRef, Collections.emptySet());
    }

    // Private helper methods

    private void buildIndex(SpreadsheetLayout<E> layout, java.util.function.Function<E, Object> entityKeyProvider) {
        for (CellBinding<E> binding : layout.getCellBindings()) {
            CellRef cellRef = new DefaultCellRef(binding.getRowIndex(), binding.getColumnIndex());

            // Store cellRef → CellBinding mapping
            cellRefToBinding.put(cellRef, binding);

            // Handle entity-based cells (flat table)
            E entity = binding.getEntityRef();
            if (entity != null) {
                Object entityKey = entityKeyProvider.apply(entity);
                entityKeyToCellRefs.computeIfAbsent(entityKey, k -> new HashSet<>()).add(cellRef);
            }

            // Handle pivot cells
            if (binding.getPivotContext() != null) {
                // For pivot cells, track contributing entity keys from source entities
                Set<Object> entityKeys = new HashSet<>();
                if (binding instanceof PivotCellBinding) {
                    PivotCellBinding<E> pivotBinding =
                            (PivotCellBinding<E>) binding;
                    for (E sourceEntity : pivotBinding.getSourceEntities()) {
                        Object sourceEntityKey = entityKeyProvider.apply(sourceEntity);
                        entityKeys.add(sourceEntityKey);
                        // Also add this pivot cell to entityKeyToCellRefs for efficient lookup
                        entityKeyToCellRefs.computeIfAbsent(sourceEntityKey, k -> new HashSet<>()).add(cellRef);
                    }
                }
                pivotCellToEntityKeys.put(cellRef, entityKeys);
            }
        }
    }

    /**
     * Default implementation of CellRef.
     */
    private static class DefaultCellRef implements CellRef {
        private final int row;
        private final int column;

        DefaultCellRef(int row, int column) {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefaultCellRef that = (DefaultCellRef) o;
            return row == that.row && column == that.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }
}
