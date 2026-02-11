package com.hexstyle.jmixspreadsheet.diff;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetPivot;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates layout changes to determine if incremental updates are possible
 * or if a full rerender is required.
 * <p>
 * This validator detects structural invalidation conditions such as:
 * - Pivot axis key changes
 * - Sorting changes
 * - Expand/collapse pivot operations
 * <p>
 * The validator provides reasons for requiring full rerender, which can be
 * logged for debugging and monitoring purposes.
 *
 * @param <E> the entity type
 */
public class LayoutValidator<E> {

    private final SpreadsheetTableModel<E> model;

    /**
     * Creates a new layout validator.
     *
     * @param model the table model
     */
    public LayoutValidator(SpreadsheetTableModel<E> model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }

        this.model = model;
    }

    /**
     * Validates a layout delta to determine if it requires a full rerender.
     * <p>
     * This method checks if the delta contains any changes that invalidate
     * the layout structure and require a full rerender rather than incremental updates.
     *
     * @param delta the layout delta to validate
     * @param oldEntity the old entity state (snapshot)
     * @param newEntity the new entity state
     * @param layoutIndex the layout index
     * @return the validation result
     */
    public LayoutValidationResult<E> validateDelta(
            LayoutDelta delta,
            E oldEntity,
            E newEntity,
            LayoutIndex<E> layoutIndex) {

        if (delta == null) {
            throw new IllegalArgumentException("Delta cannot be null");
        }
        if (oldEntity == null) {
            throw new IllegalArgumentException("Old entity cannot be null");
        }
        if (newEntity == null) {
            throw new IllegalArgumentException("New entity cannot be null");
        }
        if (layoutIndex == null) {
            throw new IllegalArgumentException("Layout index cannot be null");
        }

        List<String> reasons = new ArrayList<>();

        // Check for pivot axis key changes
        model.getPivot().ifPresent(pivot -> {
            if (hasPivotAxisKeyChanged(oldEntity, newEntity, pivot)) {
                reasons.add("Pivot axis key changed");
            }
        });

        // Check for structural changes in delta
        if (!delta.getRowsToInsert().isEmpty() || !delta.getRowsToRemove().isEmpty()) {
            reasons.add("Row structure changed (rows inserted or removed)");
        }

        // If no reasons found, incremental updates are possible
        if (reasons.isEmpty()) {
            return LayoutValidationResult.incrementalUpdate();
        } else {
            return LayoutValidationResult.fullRerender(reasons);
        }
    }

    /**
     * Validates if entity addition/removal requires full rerender.
     * <p>
     * For pivot tables, adding or removing entities typically requires full rerender
     * because it may change the axis structure. For flat tables, it may be possible
     * to do incremental updates in some cases.
     *
     * @param isAddition whether this is an entity addition (true) or removal (false)
     * @return the validation result
     */
    public LayoutValidationResult<E> validateEntityStructureChange(boolean isAddition) {
        List<String> reasons = new ArrayList<>();

        // For pivot tables, entity addition/removal typically requires full rerender
        if (model.getPivot().isPresent()) {
            String operation = isAddition ? "added" : "removed";
            reasons.add("Entity " + operation + " in pivot table");
        } else {
            // For flat tables, we could potentially do incremental updates
            // For now, we require full rerender for simplicity
            String operation = isAddition ? "added" : "removed";
            reasons.add("Entity " + operation + " in flat table");
        }

        return LayoutValidationResult.fullRerender(reasons);
    }

    /**
     * Checks if any pivot axis key has changed between old and new entity states.
     *
     * @param oldEntity the old entity state
     * @param newEntity the new entity state
     * @param pivot the pivot configuration
     * @return {@code true} if any axis key changed, {@code false} otherwise
     */
    private boolean hasPivotAxisKeyChanged(E oldEntity, E newEntity, SpreadsheetPivot<E> pivot) {
        // Check row axes
        for (var axis : pivot.getRowAxes()) {
            Object oldKey = axis.getKeyProvider().apply(oldEntity);
            Object newKey = axis.getKeyProvider().apply(newEntity);
            if (!Objects.equals(oldKey, newKey)) {
                return true;
            }
        }

        // Check column axes
        for (var axis : pivot.getColumnAxes()) {
            Object oldKey = axis.getKeyProvider().apply(oldEntity);
            Object newKey = axis.getKeyProvider().apply(newEntity);
            if (!Objects.equals(oldKey, newKey)) {
                return true;
            }
        }

        return false;
    }
}
