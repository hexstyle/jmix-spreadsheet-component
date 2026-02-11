package com.hexstyle.jmixspreadsheet.api;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Controller for managing a spreadsheet component bound to entity data.
 * <p>
 * The controller coordinates between the table model, data source, and the underlying
 * spreadsheet component. It handles data binding, incremental updates, and user interactions.
 * <p>
 * The controller uses DataManager for entity persistence and Metadata for entity creation.
 *
 * @param <E> the entity type
 * @param <DC> the data container type (typically CollectionContainer)
 */
public interface SpreadsheetController<E, DC> {

    /**
     * Binds the spreadsheet to a table model and data source.
     * <p>
     * This method establishes the connection between the declarative table model
     * and the data container. After binding, the spreadsheet will display data
     * from the container according to the model configuration.
     *
     * @param model the table model defining the spreadsheet structure
     * @param dataContainer the data container providing entity data
     * @throws IllegalStateException if already bound
     * @throws IllegalArgumentException if the model is incompatible with the container
     */
    void bind(SpreadsheetTableModel<E> model, DC dataContainer);

    /**
     * Reloads data from the data source and refreshes the spreadsheet.
     * <p>
     * This method triggers a refresh of the spreadsheet display, fetching
     * the latest data from the bound data container and applying any changes
     * according to the table model.
     *
     * @throws IllegalStateException if not yet bound
     */
    void reload();

    /**
     * Returns the underlying spreadsheet component.
     * <p>
     * The component type is determined by the generic type parameter {@code C}.
     * Implementations should return the actual component instance used for rendering.
     *
     * @return the spreadsheet component
     * @throws IllegalStateException if not yet bound
     */
    Spreadsheet getComponent();

    /**
     * Saves all pending changes to the database.
     * <p>
     * This method uses DataManager to save entities and updates the data container
     * with the saved instances (including fresh values and new IDs).
     * <p>
     * This method should be called after cell edits to ensure changes are persisted.
     * The controller automatically calls this method after each cell edit, but it can
     * also be called manually to save all pending changes at once.
     *
     * @throws IllegalStateException if not yet bound
     * @throws RuntimeException if save fails (e.g., validation errors)
     */
    void save();

    /**
     * Updates affected cells in the spreadsheet based on affected entity IDs.
     * <p>
     * This method can be called after edits via services (e.g., ShipmentDataManagerService)
     * to incrementally update only the affected cells without a full rerender.
     * <p>
     * The method will:
     * 1. Find affected entities in the data container
     * 2. Analyze changes using ChangeAnalyzer
     * 3. Compute LayoutDelta
     * 4. Apply incremental updates to affected cells
     *
     * @param affectedEntityKeys the keys of entities that were affected by the edit
     * @throws IllegalStateException if not yet bound
     */
    void updateAffectedCells(java.util.Set<Object> affectedEntityKeys);

    /**
     * Sets read-only mode for the spreadsheet.
     * <p>
     * When read-only mode is enabled, cell edits are rejected and reverted.
     *
     * @param readOnly whether the spreadsheet is read-only
     */
    default void setReadOnly(boolean readOnly) {
        // Default no-op for controllers that don't support read-only mode.
    }

    /**
     * Returns whether the spreadsheet is in read-only mode.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    default boolean isReadOnly() {
        return false;
    }

    /**
     * Sets visibility for the navigation grid (row/column headings).
     *
     * @param visible whether row/column headings should be visible
     */
    default void setNavigationGridVisible(boolean visible) {
        // Default no-op for controllers that don't support navigation grid visibility.
    }

    /**
     * Returns whether the navigation grid (row/column headings) is visible.
     *
     * @return {@code true} if navigation grid is visible, {@code false} otherwise
     */
    default boolean isNavigationGridVisible() {
        return true;
    }
}
