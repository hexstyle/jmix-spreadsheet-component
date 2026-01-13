package com.company.jmixspreadsheet.spreadsheet.diff;

import com.company.jmixspreadsheet.spreadsheet.layout.SpreadsheetLayout;

/**
 * Applies incremental updates (patches) to the spreadsheet component.
 * <p>
 * The patch applier updates only the changed cells, applying values and styles,
 * without performing a full rerender. Falls back to full rerender if structure
 * is invalidated (e.g., pivot axis key changed, sorting changed, expand/collapse).
 *
 * @param <E> the entity type
 * @param <C> the component type
 */
public interface PatchApplier<E, C> {

    /**
     * Applies a layout delta as an incremental patch to the spreadsheet component.
     *
     * @param component the spreadsheet component to update
     * @param delta the layout delta describing the changes
     * @param currentLayout the current layout structure
     * @return {@code true} if patch was applied successfully, {@code false} if full rerender is needed
     */
    boolean applyPatch(C component, LayoutDelta delta, SpreadsheetLayout<E> currentLayout);
}
