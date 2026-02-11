package com.hexstyle.jmixspreadsheet.diff;

import com.hexstyle.jmixspreadsheet.api.PivotAxis;
import com.hexstyle.jmixspreadsheet.api.PivotMeasure;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetPivot;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex.CellRef;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;

import java.util.*;
import java.util.function.Function;

/**
 * Default implementation of {@link ChangeAnalyzer} for flat and pivot tables.
 * <p>
 * This analyzer compares old and new entity states and determines which cells
 * need to be updated based on changed property values. It handles both flat
 * tables and pivot tables.
 * <p>
 * For pivot tables:
 * - Detects axis key changes (requires full rerender - signaled by empty result)
 * - Identifies affected pivot cells for entity changes
 * - Only recalculates impacted measures
 *
 * @param <E> the entity type
 */
public class DefaultChangeAnalyzer<E> implements ChangeAnalyzer<E> {

    private final SpreadsheetTableModel<E> model;
    private final Function<E, Object> entityKeyProvider;

    /**
     * Creates a new change analyzer for the given table model.
     *
     * @param model the table model defining columns
     * @param entityKeyProvider function that extracts entity keys from entities
     */
    public DefaultChangeAnalyzer(
            SpreadsheetTableModel<E> model,
            Function<E, Object> entityKeyProvider) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (entityKeyProvider == null) {
            throw new IllegalArgumentException("Entity key provider cannot be null");
        }

        this.model = model;
        this.entityKeyProvider = entityKeyProvider;
    }

    @Override
    public LayoutDelta analyzeChanges(E oldEntity, E newEntity, LayoutIndex<E> layoutIndex) {
        if (oldEntity == null) {
            throw new IllegalArgumentException("Old entity cannot be null");
        }
        if (newEntity == null) {
            throw new IllegalArgumentException("New entity cannot be null");
        }
        if (layoutIndex == null) {
            throw new IllegalArgumentException("Layout index cannot be null");
        }

        // Check if this is a pivot table
        Optional<SpreadsheetPivot<E>> pivot = model.getPivot();
        if (pivot.isPresent()) {
            return analyzePivotChanges(oldEntity, newEntity, layoutIndex, pivot.get());
        } else {
            return analyzeFlatTableChanges(oldEntity, newEntity, layoutIndex);
        }
    }

    /**
     * Analyzes changes for flat tables.
     */
    private LayoutDelta analyzeFlatTableChanges(E oldEntity, E newEntity, LayoutIndex<E> layoutIndex) {
        // Get entity key
        Object entityKey = entityKeyProvider.apply(newEntity);

        // Get all cell references for this entity
        Set<CellRef> allCellRefs = layoutIndex.getCellRefs(entityKey);
        if (allCellRefs.isEmpty()) {
            // Entity not found in layout index - no changes to report
            return DefaultLayoutDelta.empty();
        }

        // Find cells that need updating by comparing old and new values
        Set<CellRef> cellsToUpdate = new HashSet<>();

        for (CellRef cellRef : allCellRefs) {
            CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
            if (binding == null || binding.getEntityRef() == null) {
                // Skip cells that don't have entity bindings (e.g., pivot cells, header cells)
                continue;
            }

            // Check if this cell's value has changed
            if (hasCellValueChanged(binding, oldEntity, newEntity)) {
                cellsToUpdate.add(cellRef);
            }
        }

        return new DefaultLayoutDelta(cellsToUpdate);
    }

    /**
     * Analyzes changes for pivot tables.
     */
    private LayoutDelta analyzePivotChanges(
            E oldEntity,
            E newEntity,
            LayoutIndex<E> layoutIndex,
            SpreadsheetPivot<E> pivot) {

        // Check if any axis key changed
        if (hasAnyAxisKeyChanged(oldEntity, newEntity, pivot)) {
            // Axis key changed - requires full rerender
            // Return empty delta as signal that full rerender is needed
            // The caller (controller) should detect this and trigger full rerender
            return DefaultLayoutDelta.empty();
        }

        // Get entity key
        Object entityKey = entityKeyProvider.apply(newEntity);

        // Find all pivot cells that contain this entity
        Set<CellRef> affectedPivotCells = findAffectedPivotCells(entityKey, layoutIndex, pivot);

        // Filter to only cells with impacted measures
        Set<CellRef> cellsToUpdate = filterByImpactedMeasures(affectedPivotCells, oldEntity, newEntity, layoutIndex, pivot);

        return new DefaultLayoutDelta(cellsToUpdate);
    }

    /**
     * Checks if a cell's value has changed between old and new entity states.
     * <p>
     * This method extracts the value from both entities using the column's
     * value provider and compares them for equality.
     *
     * @param binding the cell binding containing column information
     * @param oldEntity the old entity state
     * @param newEntity the new entity state
     * @return {@code true} if the value has changed, {@code false} otherwise
     */
    private boolean hasCellValueChanged(CellBinding<E> binding, E oldEntity, E newEntity) {
        // Find the column for this cell
        int columnIndex = binding.getColumnIndex();
        SpreadsheetColumn<E> column = findColumnByIndex(columnIndex);
        if (column == null) {
            // Column not found - cannot determine if changed
            return false;
        }

        // Extract values using the column's value provider
        Object oldValue = column.getValueProvider().apply(oldEntity);
        Object newValue = column.getValueProvider().apply(newEntity);

        // Compare values for equality
        return !Objects.equals(oldValue, newValue);
    }

    /**
     * Finds the column at the given index.
     * <p>
     * The column index corresponds to the position in the model's column list.
     *
     * @param columnIndex the column index (0-based)
     * @return the column at that index, or {@code null} if index is out of bounds
     */
    private SpreadsheetColumn<E> findColumnByIndex(int columnIndex) {
        java.util.List<SpreadsheetColumn<E>> columns = model.getColumns();
        if (columnIndex < 0 || columnIndex >= columns.size()) {
            return null;
        }
        return columns.get(columnIndex);
    }

    /**
     * Checks if any axis key has changed between old and new entity states.
     * <p>
     * If any axis key changed, the pivot structure is invalidated and requires
     * a full rerender rather than incremental updates.
     *
     * @param oldEntity the old entity state
     * @param newEntity the new entity state
     * @param pivot the pivot configuration
     * @return {@code true} if any axis key changed, {@code false} otherwise
     */
    private boolean hasAnyAxisKeyChanged(E oldEntity, E newEntity, SpreadsheetPivot<E> pivot) {
        // Check row axes
        for (PivotAxis<E> axis : pivot.getRowAxes()) {
            Object oldKey = axis.getKeyProvider().apply(oldEntity);
            Object newKey = axis.getKeyProvider().apply(newEntity);
            if (!Objects.equals(oldKey, newKey)) {
                return true;
            }
        }

        // Check column axes
        for (PivotAxis<E> axis : pivot.getColumnAxes()) {
            Object oldKey = axis.getKeyProvider().apply(oldEntity);
            Object newKey = axis.getKeyProvider().apply(newEntity);
            if (!Objects.equals(oldKey, newKey)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds all pivot cells that are affected by an entity change.
     * <p>
     * This method finds pivot cells that contain the entity, which will need
     * recalculation when the entity changes.
     *
     * @param entityKey the entity key
     * @param layoutIndex the layout index
     * @param pivot the pivot configuration
     * @return the set of affected pivot cell references
     */
    private Set<CellRef> findAffectedPivotCells(
            Object entityKey,
            LayoutIndex<E> layoutIndex,
            SpreadsheetPivot<E> pivot) {

        Set<CellRef> affectedCells = new HashSet<>();

        // Get all cells containing this entity (includes both flat table cells and pivot cells)
        Set<CellRef> allCells = layoutIndex.getCellRefs(entityKey);

        // Filter to only pivot cells
        for (CellRef cellRef : allCells) {
            CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
            if (binding != null && binding.getPivotContext() != null) {
                affectedCells.add(cellRef);
            }
        }

        return affectedCells;
    }

    /**
     * Filters pivot cells to only those with measures that are impacted by the entity change.
     * <p>
     * This method determines which pivot cells need recalculation based on whether
     * any measure value providers would produce different values for the old vs new entity.
     * <p>
     * Note: Since we cannot determine which specific measure each pivot cell represents
     * from the binding alone, if any measure value changed, all pivot cells containing
     * the entity are marked for update (conservative but correct approach).
     *
     * @param pivotCells the set of pivot cells to filter
     * @param oldEntity the old entity state
     * @param newEntity the new entity state
     * @param layoutIndex the layout index
     * @param pivot the pivot configuration
     * @return the set of pivot cells with impacted measures
     */
    private Set<CellRef> filterByImpactedMeasures(
            Set<CellRef> pivotCells,
            E oldEntity,
            E newEntity,
            LayoutIndex<E> layoutIndex,
            SpreadsheetPivot<E> pivot) {

        Object entityKey = entityKeyProvider.apply(newEntity);
        Set<CellRef> impactedCells = new HashSet<>();

        // Check if any measure value changed
        boolean anyMeasureChanged = false;
        List<PivotMeasure<E>> measures = pivot.getMeasures();
        for (PivotMeasure<E> measure : measures) {
            Number oldValue = measure.getValueProvider().apply(oldEntity);
            Number newValue = measure.getValueProvider().apply(newEntity);
            if (!Objects.equals(oldValue, newValue)) {
                anyMeasureChanged = true;
                break;
            }
        }

        // If any measure changed, all pivot cells containing the entity need update
        if (anyMeasureChanged) {
            for (CellRef cellRef : pivotCells) {
                CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
                if (binding == null || binding.getPivotContext() == null) {
                    continue;
                }

                // Verify this pivot cell contains the entity (by checking entity keys)
                Set<Object> cellEntityKeys = layoutIndex.getEntityKeys(cellRef);
                if (cellEntityKeys.contains(entityKey)) {
                    impactedCells.add(cellRef);
                }
            }
        }

        return impactedCells;
    }
}
