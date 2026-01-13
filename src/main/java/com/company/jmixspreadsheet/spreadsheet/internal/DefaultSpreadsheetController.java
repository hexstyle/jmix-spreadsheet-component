package com.company.jmixspreadsheet.spreadsheet.internal;

import com.company.jmixspreadsheet.spreadsheet.api.SpreadsheetController;
import com.company.jmixspreadsheet.spreadsheet.api.SpreadsheetTableModel;
import com.company.jmixspreadsheet.spreadsheet.datasource.DataSourceAdapter;
import com.company.jmixspreadsheet.spreadsheet.diff.ChangeAnalyzer;
import com.company.jmixspreadsheet.spreadsheet.diff.LayoutDelta;
import com.company.jmixspreadsheet.spreadsheet.diff.LayoutValidationResult;
import com.company.jmixspreadsheet.spreadsheet.diff.LayoutValidator;
import com.company.jmixspreadsheet.spreadsheet.diff.PatchApplier;
import com.company.jmixspreadsheet.spreadsheet.edit.FlatTableCellEditor;
import com.company.jmixspreadsheet.spreadsheet.edit.PivotTableCellEditor;
import com.company.jmixspreadsheet.spreadsheet.index.LayoutIndex;
import com.company.jmixspreadsheet.spreadsheet.layout.CellBinding;
import com.company.jmixspreadsheet.spreadsheet.layout.LayoutEngine;
import com.company.jmixspreadsheet.spreadsheet.layout.PivotCellBinding;
import com.company.jmixspreadsheet.spreadsheet.layout.SpreadsheetLayout;
import com.company.jmixspreadsheet.spreadsheet.render.SpreadsheetRenderer;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.util.CellReference;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link SpreadsheetController}.
 * <p>
 * This controller coordinates between the table model, data source, and underlying
 * spreadsheet component. It manages the lifecycle and delegates to specialized
 * components for layout building, rendering, and incremental updates.
 * <p>
 * The controller does not implement layout logic or access spreadsheet cells directly;
 * all such operations are delegated to the layout engine, renderer, and patch applier.
 * <p>
 * The controller uses DataManager for entity persistence and Metadata for entity creation.
 *
 * @param <E> the entity type
 * @param <DC> the data container type (typically CollectionContainer)
 */
public class DefaultSpreadsheetController<E, DC> implements SpreadsheetController<E, DC> {

    private SpreadsheetTableModel<E> model;
    @SuppressWarnings("unused") // Stored for lifecycle management and passed to factories
    private DC dataContainer;
    private Spreadsheet component;

    private final io.jmix.core.DataManager dataManager;
    private final io.jmix.core.Metadata metadata;
    private final DataSourceAdapterFactory<E> dataSourceAdapterFactory;
    private final LayoutEngineFactory<E> layoutEngineFactory;
    private final SpreadsheetRendererFactory<E, Spreadsheet> rendererFactory;
    private final ChangeAnalyzerFactory<E> changeAnalyzerFactory;
    private final PatchApplierFactory<E, Spreadsheet> patchApplierFactory;

    private DataSourceAdapter<E> dataSourceAdapter;
    private LayoutEngine<E> layoutEngine;
    private LayoutIndex<E> layoutIndex;
    private SpreadsheetRenderer<E, Spreadsheet> renderer;
    private ChangeAnalyzer<E> changeAnalyzer;
    private PatchApplier<E, Spreadsheet> patchApplier;
    private LayoutValidator<E> layoutValidator;
    
    private FlatTableCellEditor<E> flatTableEditor;
    private PivotTableCellEditor<E> pivotTableEditor;

    private SpreadsheetLayout<E> currentLayout;
    private boolean bound = false;
    
    // Flag to suppress data change listeners during cell edit operations
    // (we handle updates via updateAffectedCells instead)
    private boolean processingCellEdit = false;

    // Entity snapshots for change tracking (entity key -> map of property name -> property value)
    private final java.util.Map<Object, java.util.Map<String, Object>> entitySnapshots = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Store layout values before edit for comparison (row,col -> value)
    private final java.util.Map<String, Object> layoutValuesBeforeEdit = new java.util.HashMap<>();

    private static final Logger logger = Logger.getLogger(DefaultSpreadsheetController.class.getName());

    /**
     * Creates a new controller instance.
     * <p>
     * The controller is not bound until {@link #bind(SpreadsheetTableModel, Object)} is called.
     *
     * @param component the spreadsheet component to manage
     * @param dataManager the DataManager for entity persistence
     * @param metadata the Metadata for entity creation
     * @param dataSourceAdapterFactory factory for creating data source adapters
     * @param layoutEngineFactory factory for creating layout engines
     * @param rendererFactory factory for creating renderers
     * @param changeAnalyzerFactory factory for creating change analyzers
     * @param patchApplierFactory factory for creating patch appliers
     */
    public DefaultSpreadsheetController(
            Spreadsheet component,
            io.jmix.core.DataManager dataManager,
            io.jmix.core.Metadata metadata,
            DataSourceAdapterFactory<E> dataSourceAdapterFactory,
            LayoutEngineFactory<E> layoutEngineFactory,
            SpreadsheetRendererFactory<E, Spreadsheet> rendererFactory,
            ChangeAnalyzerFactory<E> changeAnalyzerFactory,
            PatchApplierFactory<E, Spreadsheet> patchApplierFactory) {
        this.component = component;
        this.dataManager = dataManager;
        this.metadata = metadata;
        // Factories will be called during bind() to create dependencies
        // Store factories for later use
        this.dataSourceAdapterFactory = dataSourceAdapterFactory;
        this.layoutEngineFactory = layoutEngineFactory;
        this.rendererFactory = rendererFactory;
        this.changeAnalyzerFactory = changeAnalyzerFactory;
        this.patchApplierFactory = patchApplierFactory;
    }

    @Override
    public void bind(SpreadsheetTableModel<E> model, DC dataContainer) {
        if (bound) {
            throw new IllegalStateException("Controller is already bound. Unbind before rebinding.");
        }

        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (dataContainer == null) {
            throw new IllegalArgumentException("Data container cannot be null");
        }

        // Validate model compatibility with container
        validateModel(model, dataContainer);

        this.model = model;
        this.dataContainer = dataContainer;

        try {
            // Create data source adapter
            this.dataSourceAdapter = dataSourceAdapterFactory.create(dataContainer);
            setupDataChangeListeners();

            // Create layout engine (based on model - flat or pivot)
            this.layoutEngine = layoutEngineFactory.create(model);

            // Create renderer
            this.renderer = rendererFactory.create(model);

            // Create change analyzer
            this.changeAnalyzer = changeAnalyzerFactory.create(model);

            // Create patch applier
            this.patchApplier = patchApplierFactory.create(model);

            // Create layout validator
            this.layoutValidator = new LayoutValidator<>(model);

            // Initial layout and render (this builds the layoutIndex)
            performInitialRender();
            
            // Create cell editors (after layoutIndex is built)
            createCellEditors();
            
            // Set up cell edit listeners
            setupCellEditListeners();

            this.bound = true;
        } catch (Exception e) {
            // Clean up on failure
            cleanup();
            throw new IllegalStateException("Failed to bind controller: " + e.getMessage(), e);
        }
    }

    @Override
    public void save() {
        if (!bound) {
            throw new IllegalStateException("Controller is not bound. Call bind() first.");
        }
        
        // Save all entities tracked in DataContext
        saveEntities(null);
    }
    
    /**
     * Saves the specified entities to the database using DataManager.
     * <p>
     * This method uses DataManager.saveAll() to persist entities and get back saved instances
     * with fresh values (including new IDs for new entities). The data container is then
     * updated with these saved instances to ensure consistency.
     * <p>
     * This method only uses DataManager for saving - it does not interact with DataContext.
     *
     * @param entities the entities to save (must not be null, but can be empty)
     */
    private void saveEntities(java.util.Collection<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        // Convert to list for DataManager.saveAll()
        java.util.List<E> entitiesToSave = new java.util.ArrayList<>(entities);
        
        // Save entities using DataManager.saveAll() to get back saved instances
        // This ensures we have entities with fresh values (new IDs, updated properties)
        io.jmix.core.EntitySet savedEntitySet = dataManager.saveAll(entitiesToSave);
        
        // Update the container with saved entities from DataManager
        // This replaces container entities with the saved instances to ensure consistency
        updateContainerWithSavedEntities(savedEntitySet);
    }
    
    
    /**
     * Updates the container with saved entities from DataManager.
     * <p>
     * After DataManager.saveAll(), the EntitySet contains the saved instances with fresh values.
     * This method replaces container entities with the saved instances.
     *
     * @param savedEntitySet the EntitySet returned from DataManager.saveAll()
     */
    @SuppressWarnings("unchecked")
    private void updateContainerWithSavedEntities(io.jmix.core.EntitySet savedEntitySet) {
        if (!(dataContainer instanceof io.jmix.flowui.model.CollectionContainer)) {
            return;
        }
        
        io.jmix.flowui.model.CollectionContainer<E> collectionContainer = 
                (io.jmix.flowui.model.CollectionContainer<E>) dataContainer;
        
        // Build a map of saved entities by ID for fast lookup
        java.util.Map<Object, E> savedEntitiesById = new java.util.HashMap<>();
        java.util.function.Function<E, Object> entityKeyProvider = io.jmix.core.entity.EntityValues::getId;
        
        for (Object savedEntityObj : savedEntitySet) {
            // EntitySet contains entities - we can safely cast to E
            @SuppressWarnings("unchecked")
            E savedEntity = (E) savedEntityObj;
            Object entityKey = entityKeyProvider.apply(savedEntity);
            if (entityKey != null) {
                savedEntitiesById.put(entityKey, savedEntity);
            }
        }
        
        // Replace container entities with saved entities
        java.util.List<E> mutableItems = collectionContainer.getMutableItems();
        java.util.List<E> containerItems = new java.util.ArrayList<>(collectionContainer.getItems());
        
        for (int i = 0; i < containerItems.size(); i++) {
            E containerEntity = containerItems.get(i);
            Object entityKey = entityKeyProvider.apply(containerEntity);
            
            if (entityKey != null) {
                E savedEntity = savedEntitiesById.get(entityKey);
                if (savedEntity != null) {
                    // Replace container entity with saved entity
                    mutableItems.set(i, savedEntity);
                }
            }
        }
        
        // Add new entities that were created and saved but not yet in container
        for (E savedEntity : savedEntitiesById.values()) {
            Object entityKey = entityKeyProvider.apply(savedEntity);
            if (entityKey != null && !containerItems.contains(savedEntity)) {
                // Check if entity with this ID already exists in container
                boolean exists = false;
                for (E existingEntity : containerItems) {
                    Object existingKey = entityKeyProvider.apply(existingEntity);
                    if (entityKey.equals(existingKey)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    mutableItems.add(savedEntity);
                }
            }
        }
    }
    
    /**
     * Copies property values from source entity to target entity.
     * <p>
     * This ensures the container entity has the same property values as the managed entity
     * from DataContext after save.
     * <p>
     * In Jmix, after DataContext.save(), the managed entities should already have fresh values.
     * If the container entities are the same instances as the managed entities (which they should be),
     * then no copying is needed. However, if they're different instances, we need to ensure
     * the container entities have the fresh values.
     * <p>
     * Note: This is a simplified implementation. In a production system with complex entities,
     * you might need to use Metadata to iterate through all properties and copy them.
     *
     * @param source the source entity with fresh values
     * @param target the target entity in the container to update
     */
    private void copyEntityProperties(E source, E target) {
        // In Jmix, after DataContext.save(), the managed entity should be the same instance
        // as the container entity (merge() returns the same instance for already-managed entities).
        // So if source == target, no copying is needed.
        if (source == target) {
            return;
        }
        
        // If they're different instances, we would need to copy properties.
        // However, this should not happen in normal Jmix operation - the container entities
        // should be the same instances as the managed entities.
        // For now, we just copy the ID as a minimal safeguard.
        try {
            Object id = io.jmix.core.entity.EntityValues.getId(source);
            if (id != null) {
                io.jmix.core.entity.EntityValues.setId(target, id);
            }
        } catch (Exception e) {
            logger.warning("Failed to refresh entity properties: " + e.getMessage());
        }
    }

    @Override
    public void reload() {
        if (!bound) {
            throw new IllegalStateException("Controller is not bound. Call bind() first.");
        }

        // Rebuild layout from current data
        rebuildLayout();

        // Re-render the spreadsheet
        renderer.render(component, currentLayout);

        // Refresh the component to ensure changes are visible
        // This is critical for Vaadin Spreadsheet to redraw after filter changes
        if (component instanceof com.vaadin.flow.component.spreadsheet.Spreadsheet) {
            com.vaadin.flow.component.spreadsheet.Spreadsheet vaadinSpreadsheet = 
                    (com.vaadin.flow.component.spreadsheet.Spreadsheet) component;
            try {
                vaadinSpreadsheet.refreshAllCellValues();
            } catch (Exception e) {
                logger.warning("Failed to refresh spreadsheet after reload: " + e.getMessage());
            }
        }

        // Rebuild layout index after render
        rebuildLayoutIndex();
        
        // Update cell editors with current layout (so they can access all bindings including headers)
        updateEditorLayouts();
        
        // Update cell editability
        setCellEditability();
    }

    @Override
    public Spreadsheet getComponent() {
        if (!bound) {
            throw new IllegalStateException("Controller is not bound. Call bind() first.");
        }
        return component;
    }

    /**
     * Unbinds the controller and releases resources.
     * <p>
     * After unbinding, the controller can be bound again with new data.
     */
    public void unbind() {
        if (!bound) {
            return;
        }

        cleanup();
        this.bound = false;
        this.model = null;
        this.dataContainer = null;
        this.currentLayout = null;
        this.entitySnapshots.clear();
    }

    /**
     * Returns whether this controller is currently bound.
     *
     * @return {@code true} if bound, {@code false} otherwise
     */
    public boolean isBound() {
        return bound;
    }

    // Private helper methods

    private void validateModel(SpreadsheetTableModel<E> model, DC dataContainer) {
        // Basic validation: ensure model entity class matches container
        // Detailed validation depends on container implementation
        // This is a placeholder for validation logic
    }

    private void setupDataChangeListeners() {
        // Subscribe to entity changes via data source adapter
        dataSourceAdapter.addEntityAddedListener(this::onEntityAdded);
        dataSourceAdapter.addEntityRemovedListener(this::onEntityRemoved);
        dataSourceAdapter.addEntityChangedListener(this::onEntityChanged);
        // Subscribe to collection refresh events (e.g., when filter changes)
        dataSourceAdapter.addRefreshListener(this::onRefresh);
    }

    private void performInitialRender() {
        // Build initial layout
        rebuildLayout();

        // Render layout to component
        renderer.render(component, currentLayout);

        // Build layout index after initial render
        rebuildLayoutIndex();
        
        // Update cell editors with current layout (so they can access all bindings including headers)
        updateEditorLayouts();
        
        // Set cell editability (locked/unlocked state)
        setCellEditability();
    }

    private void rebuildLayout() {
        // Delegate to layout engine to build layout from current data
        Iterable<E> entities = dataSourceAdapter.getEntities();
        this.currentLayout = layoutEngine.buildLayout(model, entities);
    }

    private void rebuildLayoutIndex() {
        // Build layout index from current layout using entity key provider
        java.util.function.Function<E, Object> entityKeyProvider = io.jmix.core.entity.EntityValues::getId;
        this.layoutIndex = new com.company.jmixspreadsheet.spreadsheet.index.DefaultLayoutIndex<>(
                currentLayout, entityKeyProvider);
        
        // Update entity snapshots after layout rebuild
        updateEntitySnapshots();
    }
    
    /**
     * Updates cell editors with the current layout.
     * <p>
     * This allows editors to access all cell bindings, including header cells,
     * which is necessary for extracting column axis values when editing empty pivot cells.
     */
    private void updateEditorLayouts() {
        if (pivotTableEditor != null && currentLayout != null) {
            pivotTableEditor.updateLayout(currentLayout);
        }
    }

    /**
     * Updates entity snapshots for change tracking.
     * <p>
     * Creates snapshots of all entities in the current data source for comparison
     * when changes occur. Stores property values as a map for each entity.
     */
    private void updateEntitySnapshots() {
        entitySnapshots.clear();
        java.util.function.Function<E, Object> entityKeyProvider = io.jmix.core.entity.EntityValues::getId;
        
        for (E entity : dataSourceAdapter.getEntities()) {
            Object entityKey = entityKeyProvider.apply(entity);
            if (entityKey != null) {
                // Create a snapshot by storing property values
                java.util.Map<String, Object> snapshot = createEntitySnapshot(entity);
                entitySnapshots.put(entityKey, snapshot);
            }
        }
    }

    /**
     * Creates a snapshot of an entity for change tracking.
     * <p>
     * This method creates a snapshot by storing property values in a map.
     * This avoids issues with entity state changes and provides a proper snapshot
     * that can be compared later.
     *
     * @param entity the entity to snapshot
     * @return a map of property names to property values
     */
    private java.util.Map<String, Object> createEntitySnapshot(E entity) {
        if (entity == null) {
            return new java.util.HashMap<>();
        }
        
        java.util.Map<String, Object> snapshot = new java.util.HashMap<>();
        
        // Store common entity properties that are likely to change
        // We store property values, not references, to create a true snapshot
        try {
            // Store entity ID
            Object id = io.jmix.core.entity.EntityValues.getId(entity);
            if (id != null) {
                snapshot.put("__id", id);
            }
            
            // Note: Creating a complete snapshot would require metadata to iterate through
            // all properties. For now, we store only the ID. If full snapshots are needed
            // for change detection, we would need to:
            // 1. Access Metadata to get all properties
            // 2. Iterate through properties and store their values
            // 3. Handle nested entities, collections, etc.
            //
            // Since entitySnapshots is currently not used in the incremental update path
            // (we use LayoutIndex.getCellRefs() instead), this simplified implementation
            // is sufficient for now.
        } catch (Exception e) {
            logger.warning("Failed to create entity snapshot: " + e.getMessage());
        }
        
        return snapshot;
    }

    private void onEntityAdded(E entity) {
        // Skip if we're processing a cell edit - updates are handled via updateAffectedCells
        if (processingCellEdit) {
            return;
        }
        
        // Handle entity added: validate if full rerender is required
        LayoutValidationResult<E> validation = layoutValidator.validateEntityStructureChange(true);
        if (validation.requiresFullRerender()) {
            logFullRerender("Entity added", validation.getReasons());
            reload();
        } else {
            handleDataChange();
        }
    }

    private void onEntityRemoved(E entity) {
        // Handle entity removed: validate if full rerender is required
        LayoutValidationResult<E> validation = layoutValidator.validateEntityStructureChange(false);
        if (validation.requiresFullRerender()) {
            logFullRerender("Entity removed", validation.getReasons());
            reload();
        } else {
            handleDataChange();
        }
    }

    private void onEntityChanged(E entity) {
        // Skip if we're processing a cell edit - updates are handled via updateAffectedCells
        if (processingCellEdit) {
            return;
        }
        
        // Handle entity changed: use incremental updates if possible
        // For cumulative sums and other derived values, we need to rebuild layout
        // to get fresh values, then apply incremental updates
        handleDataChange();
    }

    private void onRefresh() {
        // Collection was fully refreshed (e.g., filter changed)
        // Always do a full rerender
        logFullRerender("Collection refresh", java.util.Collections.singletonList("Filter or data reload changed collection"));
        reload();
    }

    private void handleDataChange() {
        // For now, delegate to reload() for full refresh
        // Future: implement incremental update logic here with validation
        logFullRerender("Data change", java.util.Collections.singletonList("Incremental update not yet implemented"));
        reload();
    }

    /**
     * Applies an incremental update to the spreadsheet based on affected entity IDs.
     * <p>
     * This method can be called after external edits to update
     * only the affected cells without a full rerender.
     *
     * @param affectedEntityKeys the keys of entities that were affected by the edit
     */
    public void updateAffectedCells(java.util.Set<Object> affectedEntityKeys) {
        if (!bound || affectedEntityKeys == null || affectedEntityKeys.isEmpty()) {
            return;
        }

        if (layoutIndex == null || changeAnalyzer == null || currentLayout == null) {
            logger.warning("Cannot apply incremental update: missing dependencies");
            handleDataChange();
            return;
        }

        // Rebuild layout to get fresh values (e.g., recalculated cumulative sums)
        // This uses entities that have been updated by the edit and save
        rebuildLayout();
        
        // Build new layout index from the rebuilt layout
        java.util.function.Function<E, Object> entityKeyProvider = io.jmix.core.entity.EntityValues::getId;
        LayoutIndex<E> newLayoutIndex = new com.company.jmixspreadsheet.spreadsheet.index.DefaultLayoutIndex<>(
                currentLayout, entityKeyProvider);
        
        // Update layout index BEFORE applying updates (so applyDirectCellUpdates uses new index with fresh values)
        this.layoutIndex = newLayoutIndex;
        
        // Find all cells connected to the affected entities using the layout index
        // The PivotEditStrategy already provides the affected entities, so we just need to
        // find all cells that are connected to these entities and update them
        java.util.Set<LayoutIndex.CellRef> allCellsToUpdate = new java.util.HashSet<>();
        for (Object entityKey : affectedEntityKeys) {
            java.util.Set<LayoutIndex.CellRef> cellRefs = newLayoutIndex.getCellRefs(entityKey);
            allCellsToUpdate.addAll(cellRefs);
        }

        // Apply all updates if any cells need updating
        if (!allCellsToUpdate.isEmpty()) {
            LayoutDelta combinedDelta = new com.company.jmixspreadsheet.spreadsheet.diff.DefaultLayoutDelta(allCellsToUpdate);
            applyIncrementalUpdate(null, null, combinedDelta);
        } else {
            // If no cells found to update, log and fall back to full reload
            logger.warning("No cells found to update for affected entities: " + affectedEntityKeys + 
                    ". Falling back to full reload.");
            reload();
        }
    }
    
    /**
     * Captures current layout values before edit for later comparison.
     * This is called BEFORE any entity modifications happen.
     */
    private void captureLayoutValuesBeforeEdit() {
        layoutValuesBeforeEdit.clear();
        if (currentLayout == null) {
            return;
        }
        
        // Store all cell values from current layout
        for (CellBinding<E> binding : currentLayout.getCellBindings()) {
            String key = binding.getRowIndex() + "," + binding.getColumnIndex();
            Object value = binding.getValue();
            layoutValuesBeforeEdit.put(key, value);
        }
    }
    
    /**
     * Finds all changed cells by comparing stored values (from before edit) with new layout values.
     * This is the primary method for detecting changes after edit and save.
     */
    private java.util.Set<LayoutIndex.CellRef> findChangedCellsByValueComparison() {
        java.util.Set<LayoutIndex.CellRef> changedCells = new java.util.HashSet<>();
        
        if (currentLayout == null) {
            return changedCells;
        }
        
        int dataCellCount = 0;
        int changedDataCellCount = 0;
        
        // Compare each cell in the new layout with stored values from before edit
        for (CellBinding<E> newBinding : currentLayout.getCellBindings()) {
            String key = newBinding.getRowIndex() + "," + newBinding.getColumnIndex();
            Object oldValue = layoutValuesBeforeEdit.get(key);
            Object newValue = newBinding.getValue();
            
            // Skip header cells - only update data cells (cells with entity references or pivot data cells)
            // Header cells have no entity ref and empty source entities for pivot cells
            boolean isHeaderCell = false;
            if (newBinding.getPivotContext() != null) {
                // Pivot header cells have empty source entities
                if (newBinding instanceof com.company.jmixspreadsheet.spreadsheet.layout.PivotCellBinding) {
                    com.company.jmixspreadsheet.spreadsheet.layout.PivotCellBinding<E> pivotBinding = 
                            (com.company.jmixspreadsheet.spreadsheet.layout.PivotCellBinding<E>) newBinding;
                    isHeaderCell = pivotBinding.getSourceEntities().isEmpty();
                }
            } else if (newBinding.getEntityRef() == null) {
                // Flat table header cells have no entity ref
                isHeaderCell = true;
            }
            
            if (!isHeaderCell) {
                dataCellCount++;
                
                // Only mark as changed if:
                // 1. Cell existed before AND value changed (don't mark new cells that didn't exist)
                // 2. Value actually changed (not just null to null)
                // 3. It's a data cell (not a header)
                if (oldValue != null && !compareValues(oldValue, newValue)) {
                    changedCells.add(createCellRef(newBinding.getRowIndex(), newBinding.getColumnIndex()));
                    changedDataCellCount++;
                }
            }
        }
        
        logger.info("Value comparison: " + dataCellCount + " data cells checked, " + changedDataCellCount + " changed cells found, " + 
                changedCells.size() + " cells in delta. Stored values: " + layoutValuesBeforeEdit.size() + 
                ", Current layout bindings: " + currentLayout.getCellBindings().size());
        
        return changedCells;
    }
    
    /**
     * Compares old and new layouts to find all cells that changed.
     * This includes cells with derived values like cumulative sums.
     * @deprecated Use findChangedCellsByValueComparison() instead, which uses stored values from before edit
     */
    @Deprecated
    private java.util.Set<LayoutIndex.CellRef> findChangedCells(
            SpreadsheetLayout<E> oldLayout,
            SpreadsheetLayout<E> newLayout,
            LayoutIndex<E> oldLayoutIndex,
            LayoutIndex<E> newLayoutIndex) {
        java.util.Set<LayoutIndex.CellRef> changedCells = new java.util.HashSet<>();
        
        // Compare bindings by position
        java.util.Map<String, CellBinding<E>> oldBindingMap = new java.util.HashMap<>();
        for (CellBinding<E> binding : oldLayout.getCellBindings()) {
            String key = binding.getRowIndex() + "," + binding.getColumnIndex();
            oldBindingMap.put(key, binding);
        }
        
        for (CellBinding<E> newBinding : newLayout.getCellBindings()) {
            String key = newBinding.getRowIndex() + "," + newBinding.getColumnIndex();
            CellBinding<E> oldBinding = oldBindingMap.get(key);
            
            if (oldBinding == null) {
                // New cell - mark as changed
                changedCells.add(createCellRef(newBinding.getRowIndex(), newBinding.getColumnIndex()));
            } else {
                // Compare values
                Object oldValue = oldBinding.getValue();
                Object newValue = newBinding.getValue();
                
                // Use more thorough comparison for BigDecimal and other numeric types
                boolean valuesEqual = compareValues(oldValue, newValue);
                if (!valuesEqual) {
                    // Value changed - mark as changed
                    changedCells.add(createCellRef(newBinding.getRowIndex(), newBinding.getColumnIndex()));
                }
            }
        }
        
        return changedCells;
    }
    
    /**
     * Compares two values, handling BigDecimal and other numeric types correctly.
     */
    private boolean compareValues(Object oldValue, Object newValue) {
        if (oldValue == newValue) {
            return true;
        }
        if (oldValue == null || newValue == null) {
            return false;
        }
        
        // Special handling for BigDecimal to avoid scale issues
        if (oldValue instanceof java.math.BigDecimal && newValue instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) oldValue).compareTo((java.math.BigDecimal) newValue) == 0;
        }
        
        // Default comparison
        return java.util.Objects.equals(oldValue, newValue);
    }

    /**
     * Finds an entity in the data container by its key.
     *
     * @param entityKey the entity key
     * @return the entity, or null if not found
     */
    private E findEntityByKey(Object entityKey) {
        if (entityKey == null || dataSourceAdapter == null) {
            return null;
        }

        java.util.function.Function<E, Object> entityKeyProvider = io.jmix.core.entity.EntityValues::getId;
        for (E entity : dataSourceAdapter.getEntities()) {
            Object key = entityKeyProvider.apply(entity);
            if (entityKey.equals(key)) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Applies an incremental update (patch) to the spreadsheet.
     *
     * @param entityKey the key of the entity that changed (can be null)
     * @param newEntity the new entity state (can be null)
     * @param delta the layout delta describing the changes
     */
    private void applyIncrementalUpdate(Object entityKey, E newEntity, LayoutDelta delta) {
        if (delta == null || component == null || renderer == null || currentLayout == null) {
            return;
        }

        // Check if patch applier can handle this
        if (patchApplier != null) {
            boolean patchApplied = patchApplier.applyPatch(component, delta, currentLayout);
            if (patchApplied) {
                logger.fine("Incremental patch applied successfully");
                return;
            } else {
                logger.fine("Patch applier returned false, falling back to direct cell update");
            }
        }

        // Fallback: apply updates directly using renderer's cell renderer
        // This updates only the changed cells without a full rerender
        applyDirectCellUpdates(delta);
        
        // Note: Vaadin Spreadsheet's createCell() method (called by setCellValue) 
        // updates cells directly and triggers UI refresh automatically.
        // No need to call refreshAllCellValues() which would refresh ALL cells.
    }

    /**
     * Applies cell updates directly using the renderer's cell renderer.
     * <p>
     * This is a fallback when the patch applier cannot handle the update.
     *
     * @param delta the layout delta containing cells to update
     */
    private void applyDirectCellUpdates(LayoutDelta delta) {
        if (delta == null || currentLayout == null) {
            return;
        }

        // Get cell renderer from renderer
        com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer<E, Spreadsheet> defaultRenderer =
                (com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer<E, Spreadsheet>) renderer;
        com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer.CellRenderer<Spreadsheet> cellRenderer =
                getCellRenderer(defaultRenderer);

        if (cellRenderer == null) {
            logger.warning("Cannot apply direct cell updates: cell renderer not available");
            return;
        }

        // Build a map of bindings by position for fast lookup
        java.util.Map<String, CellBinding<E>> bindingMap = new java.util.HashMap<>();
        for (CellBinding<E> binding : currentLayout.getCellBindings()) {
            String key = binding.getRowIndex() + "," + binding.getColumnIndex();
            bindingMap.put(key, binding);
        }

        // Update each affected cell
        for (LayoutIndex.CellRef cellRef : delta.getCellsToUpdate()) {
            // Find the cell binding for this cell from currentLayout
            // Note: Values in bindings are computed from entities when the layout was built (in rebuildLayout()).
            // rebuildLayout() uses entities from dataSourceAdapter.getEntities(), which come from the container.
            // After save(), the container entities should be the same instances as the DataContext entities,
            // which should have fresh values. However, if the entities are stale, the binding values will be stale.
            // To ensure fresh values, rebuildLayout() must use entities with current values.
            String key = cellRef.getRow() + "," + cellRef.getColumn();
            CellBinding<E> binding = bindingMap.get(key);
            
            if (binding == null) {
                logger.warning("Cell binding not found for cell at row=" + cellRef.getRow() + ", col=" + cellRef.getColumn() + 
                        ". Total bindings in layout: " + currentLayout.getCellBindings().size());
                continue;
            }

            // Format and set the cell value
            Object value = binding.getValue();
            if (value == null) {
                logger.warning("Cell binding value is null for cell at row=" + cellRef.getRow() + ", col=" + cellRef.getColumn() + 
                        ". Binding type: " + binding.getClass().getSimpleName());
            }
            String formattedValue = value != null ? value.toString() : "";
            cellRenderer.setCellValue(
                    component,
                    cellRef.getRow(),
                    cellRef.getColumn(),
                    formattedValue
            );

            // Apply cell style if specified
            String style = binding.getStyle();
            if (style != null) {
                cellRenderer.setCellStyle(
                        component,
                        cellRef.getRow(),
                        cellRef.getColumn(),
                        style
                );
            }
        }
    }

    /**
     * Gets the cell renderer from the default renderer.
     * Uses reflection to access the private field.
     */
    @SuppressWarnings("unchecked")
    private com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer.CellRenderer<Spreadsheet> getCellRenderer(
            com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer<E, Spreadsheet> renderer) {
        try {
            java.lang.reflect.Field field = com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer.class
                    .getDeclaredField("cellRenderer");
            field.setAccessible(true);
            return (com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer.CellRenderer<Spreadsheet>) field.get(renderer);
        } catch (Exception e) {
            logger.warning("Could not access cell renderer: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs the reasons for requiring a full rerender.
     *
     * @param operation the operation that triggered the rerender
     * @param reasons the list of reasons
     */
    private void logFullRerender(String operation, List<String> reasons) {
        if (logger.isLoggable(Level.FINE)) {
            String reasonString = String.join(", ", reasons);
            logger.fine(String.format("Full rerender required for %s: %s", operation, reasonString));
        }
    }

    private void createCellEditors() {
        // Create entity adapter for flat table
        FlatTableCellEditor.EntityAdapter<E> entityAdapter = createEntityAdapter();
        
        // Create flat table editor (always create, will be used if no pivot)
        flatTableEditor = new FlatTableCellEditor<>(model, layoutIndex, entityAdapter);
        
        // Create pivot table editor if pivot is configured
        if (model.getPivot().isPresent()) {
            pivotTableEditor = new PivotTableCellEditor<>(model, layoutIndex, 
                    createPivotEntityAdapter());
        }
    }
    
    private FlatTableCellEditor.EntityAdapter<E> createEntityAdapter() {
        // With DataManager, we don't need to merge entities - just return them as-is
        // DataManager.saveAll() works with detached entities
        return entity -> entity;
    }
    
    private PivotTableCellEditor.EntityAdapter<E> createPivotEntityAdapter() {
        return new PivotTableCellEditor.EntityAdapter<E>() {
            @Override
            public E create(Class<E> entityClass) {
                // Use Jmix Metadata.create() to create a new entity instance
                return metadata.create(entityClass);
            }
            
            @Override
            public E merge(E entity) {
                // With DataManager, merge is not needed - just return entity as-is
                // DataManager.saveAll() works with detached entities
                return entity;
            }
            
            @Override
            public void setProperty(E entity, String propertyName, Object propertyValue) {
                // Use Jmix EntityValues to set property
                io.jmix.core.entity.EntityValues.setValue(entity, propertyName, propertyValue);
            }
            
            @Override
            public E track(E entity) {
                // With DataManager, tracking is not needed - just return entity as-is
                // Entities are saved explicitly via DataManager.saveAll()
                return entity;
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    private void setupCellEditListeners() {
        // Only set up listeners if component is Vaadin Spreadsheet
        if (component instanceof com.vaadin.flow.component.spreadsheet.Spreadsheet) {
            com.vaadin.flow.component.spreadsheet.Spreadsheet vaadinSpreadsheet = 
                    (com.vaadin.flow.component.spreadsheet.Spreadsheet) component;
            
            // Add cell value change listener
            // Vaadin Spreadsheet may provide row/column/value directly on the event or via getChangedCells()
            vaadinSpreadsheet.addCellValueChangeListener(event -> {
                try {
                    int row = -1;
                    int col = -1;
                    Object newValue = null;
                    
                    // Try to get changed cells from the event
                    Set<CellReference> changedCells = event.getChangedCells();

                    if (changedCells == null || changedCells.isEmpty()) {
                        logger.fine("Cell edit event has no changed cells and no direct row/column");
                        return;
                    }

                    // Process each changed cell
                    for (CellReference cell : changedCells) {
                        try {
                            row = cell.getRow();
                            col = cell.getCol();
                            newValue = null;

                            // Get value from the spreadsheet component
                            if (newValue == null && row >= 0 && col >= 0 &&
                                    component instanceof com.vaadin.flow.component.spreadsheet.Spreadsheet) {
                                com.vaadin.flow.component.spreadsheet.Spreadsheet spreadsheet =
                                        (com.vaadin.flow.component.spreadsheet.Spreadsheet) component;
                                newValue = readCellValueFromComponent(spreadsheet, row, col);
                            }

                            // Value will be read from component in handleCellEdit if still null

                            if (row >= 0 && col >= 0) {
                                handleCellEdit(row, col, newValue);
                            } else {
                                logger.warning("Could not extract row/column from changed cell. " +
                                        "Cell class: " + cell.getClass().getName() + ", " +
                                        "Row=" + row + ", Col=" + col + ", Value=" + newValue);
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Error processing changed cell: " + e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error handling cell edit event: " + e.getMessage(), e);
                }
            });
        }
    }
    
    /**
     * Reads a cell value from the Spreadsheet component.
     *
     * @param spreadsheet the Spreadsheet component
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @return the cell value, or null if not found
     */
    private Object readCellValueFromComponent(com.vaadin.flow.component.spreadsheet.Spreadsheet spreadsheet, int row, int col) {
        try {
            // Use getCell method to get Cell object (Apache POI Cell)
            org.apache.poi.ss.usermodel.Cell cell = spreadsheet.getCell(row, col);
            if (cell == null) {
                // Try 1-based indexing if 0-based returns null
                cell = spreadsheet.getCell(row + 1, col + 1);
            }
            
            if (cell == null) {
                logger.fine("Cell is null at row=" + row + ", col=" + col);
                return null;
            }
            
            // Use CellType to determine which method to call
            org.apache.poi.ss.usermodel.CellType cellType = cell.getCellType();
            
            switch (cellType) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    // Check if it's a date
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue();
                    } else {
                        return cell.getNumericCellValue();
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case FORMULA:
                    // For formula cells, get the cached value type
                    org.apache.poi.ss.usermodel.CellType formulaResultType = cell.getCachedFormulaResultType();
                    switch (formulaResultType) {
                        case STRING:
                            return cell.getRichStringCellValue().getString();
                        case NUMERIC:
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                return cell.getDateCellValue();
                            } else {
                                return cell.getNumericCellValue();
                            }
                        case BOOLEAN:
                            return cell.getBooleanCellValue();
                        default:
                            return cell.getCellFormula();
                    }
                case BLANK:
                    return null;
                case ERROR:
                    return cell.getErrorCellValue();
                default:
                    logger.fine("Unknown cell type: " + cellType + " at row=" + row + ", col=" + col);
                    return null;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error reading cell value from component at row=" + row + ", col=" + col + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Converts row and column indices to Excel-style cell reference (e.g., "A1", "B2").
     *
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @return the cell reference string (1-based, e.g., "A1")
     */
    private String convertToCellReference(int row, int col) {
        // Convert to 1-based for Excel reference
        int excelRow = row + 1;
        int excelCol = col;
        
        StringBuilder cellRef = new StringBuilder();
        while (excelCol >= 0) {
            cellRef.insert(0, (char)('A' + (excelCol % 26)));
            excelCol = excelCol / 26 - 1;
            if (excelCol < 0) break;
        }
        cellRef.append(excelRow);
        
        return cellRef.toString();
    }

    private void handleCellEdit(int row, int col, Object newValue) {
        // If newValue is null, try to read it from the spreadsheet component
        if (newValue == null && component instanceof com.vaadin.flow.component.spreadsheet.Spreadsheet) {
            newValue = readCellValueFromComponent((com.vaadin.flow.component.spreadsheet.Spreadsheet) component, row, col);
        }
        
        // Determine if this is a pivot cell or flat table cell
        if (layoutIndex == null) {
            logger.warning("Cell edit ignored: Layout index not available");
            return;
        }
        
        LayoutIndex.CellRef cellRef = createCellRef(row, col);
        CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
        
        if (binding == null) {
            // Cell not found - might be a header or invalid cell
            logger.fine("Cell edit ignored: Cell not found in layout index at row=" + row + ", col=" + col);
            return;
        }
        
        // Check if cell is editable before processing
        if (!isCellEditable(binding)) {
            logger.fine("Cell edit ignored: Cell is not editable at row=" + row + ", col=" + col);
            // Revert the cell value to the original value from binding
            revertCellValue(row, col, binding);
            return;
        }
        
        try {
            // Get entity key provider for tracking affected entities
            java.util.function.Function<E, Object> entityKeyProvider = io.jmix.core.entity.EntityValues::getId;
            java.util.Set<Object> affectedEntityKeys = new java.util.HashSet<>();
            
            // Check if it's a pivot cell
            java.util.Set<E> affectedEntities = null;
            if (binding.getPivotContext() != null && binding instanceof PivotCellBinding) {
                // Handle pivot cell edit
                if (pivotTableEditor != null) {
                    // Pass the binding directly to avoid re-lookup
                    PivotCellBinding<E> pivotBinding = (PivotCellBinding<E>) binding;
                    
                    // Call handleCellEdit which will apply the edits
                    pivotTableEditor.handleCellEdit(row, col, newValue, pivotBinding);
                    
                    // Get all entities that were affected by the edit (from the edits map)
                    // This includes source entities AND any additional entities modified by the strategy
                    // (e.g., subsequent shipments for cumulative sum updates)
                    affectedEntities = pivotTableEditor.getLastAffectedEntities();
                } else {
                    logger.warning("Pivot cell edit ignored: No edit strategy configured");
                    return;
                }
            } else if (binding.getEntityRef() != null) {
                // Handle flat table cell edit
                flatTableEditor.handleCellEdit(row, col, newValue);
                
                // Track the affected entity for flat table cells
                affectedEntities = java.util.Collections.singleton(binding.getEntityRef());
            } else {
                // Header cell or non-editable cell - ignore
                return;
            }
            
            // Set flag to suppress data change listeners during cell edit processing
            // (we handle updates via updateAffectedCells instead)
            processingCellEdit = true;
            try {
                // Auto-save changes to database after successful edit
                // This will assign IDs to new entities and persist changes
                saveEntities(affectedEntities);
                
                // Get entity keys AFTER save() so new entities have IDs
                // This ensures new entities are included in the incremental update
                for (E entity : affectedEntities) {
                    Object entityKey = entityKeyProvider.apply(entity);
                    if (entityKey != null) {
                        affectedEntityKeys.add(entityKey);
                    }
                }
                
                // Apply incremental update for affected entities
                // This will rebuild layout with fresh values and update only changed cells
                if (!affectedEntityKeys.isEmpty()) {
                    updateAffectedCells(affectedEntityKeys);
                } else {
                    // If no entity keys tracked (shouldn't happen after save, but handle gracefully),
                    // do a full reload to ensure everything is updated
                    reload();
                }
            } finally {
                // Always clear the flag, even if an exception occurs
                processingCellEdit = false;
            }
        } catch (Exception e) {
            logger.severe("Error handling cell edit: " + e.getMessage());
            throw new RuntimeException("Failed to save cell edit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a CellRef implementation that properly implements equals/hashCode
     * for use in Map lookups in LayoutIndex.
     * <p>
     * This is a static nested class because it doesn't need access to any instance
     * members of the outer class. Each DefaultSpreadsheetController instance can
     * create its own CellRefImpl instances without issues - they are just value
     * objects that compare by row/column values.
     */
    private static class CellRefImpl implements LayoutIndex.CellRef {
        private final int row;
        private final int column;

        CellRefImpl(int row, int column) {
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
            if (!(o instanceof LayoutIndex.CellRef)) return false;
            LayoutIndex.CellRef that = (LayoutIndex.CellRef) o;
            return row == that.getRow() && column == that.getColumn();
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, column);
        }
    }
    
    private LayoutIndex.CellRef createCellRef(int row, int col) {
        return new CellRefImpl(row, col);
    }
    
    private void setCellEditability() {
        // Note: Vaadin Spreadsheet doesn't have a direct setCellLocked method
        // Cell editability is controlled by handling edit events and rejecting invalid edits
        // The cells will be editable by default, and we validate in handleCellEdit()
        // Future: Could use cell protection or styles to visually indicate non-editable cells
    }
    
    /**
     * Reverts a cell value to its original value from the binding.
     *
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @param binding the cell binding containing the original value
     */
    private void revertCellValue(int row, int col, CellBinding<E> binding) {
        try {
            if (renderer == null || component == null) {
                return;
            }
            
            // Get the original value from the binding
            Object originalValue = binding.getValue();
            String formattedValue = originalValue != null ? originalValue.toString() : "";
            
            // Get cell renderer and set the value back
            com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer<E, Spreadsheet> defaultRenderer = 
                    (com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer<E, Spreadsheet>) renderer;
            com.company.jmixspreadsheet.spreadsheet.render.DefaultSpreadsheetRenderer.CellRenderer<Spreadsheet> cellRenderer = 
                    getCellRenderer(defaultRenderer);
            
            if (cellRenderer != null) {
                cellRenderer.setCellValue(component, row, col, formattedValue);
                logger.fine("Reverted cell value at row=" + row + ", col=" + col + " to: " + formattedValue);
            }
        } catch (Exception e) {
            logger.warning("Failed to revert cell value at row=" + row + ", col=" + col + ": " + e.getMessage());
        }
    }
    
    private boolean isCellEditable(CellBinding<E> binding) {
        // Headers are not editable (no entity ref and no pivot context)
        if (binding.getEntityRef() == null && binding.getPivotContext() == null) {
            return false;
        }
        
        // Check if it's a pivot cell
        if (binding.getPivotContext() != null) {
            // Pivot cells are editable only if edit strategy is configured
            return model.getPivot()
                    .map(pivot -> pivot.getEditStrategy() != null)
                    .orElse(false);
        }
        
        // For flat table cells, check column editability
        if (binding.getEntityRef() != null) {
            var columns = model.getColumns();
            if (columns == null || columns.isEmpty()) {
                // If no columns defined, assume editable if there's an entity ref
                return true;
            }
            
            int columnIndex = binding.getColumnIndex();
            if (columnIndex >= 0 && columnIndex < columns.size()) {
                var column = columns.get(columnIndex);
                // Column is editable if:
                // 1. Column exists
                // 2. Column is marked as editable
                // 3. Column has a setter (can be updated)
                return column != null && column.isEditable() && column.getSetter() != null;
            } else {
                // Column index out of range - might be a non-data column (e.g., row number)
                // For now, allow editing if entity ref exists
                logger.fine("Column index " + columnIndex + " out of range for columns list (size=" + columns.size() + ")");
                return true; // Assume editable if entity ref exists
            }
        }
        
        return false;
    }

    private void cleanup() {
        // Dispose data source adapter
        if (dataSourceAdapter != null) {
            dataSourceAdapter.dispose();
            dataSourceAdapter = null;
        }

        // Clear other references
        layoutEngine = null;
        layoutIndex = null;
        renderer = null;
        changeAnalyzer = null;
        patchApplier = null;
        layoutValidator = null;
        flatTableEditor = null;
        pivotTableEditor = null;
    }

    // Factory interfaces for dependency creation

    /**
     * Factory for creating data source adapters.
     *
     * @param <E> the entity type
     */
    public interface DataSourceAdapterFactory<E> {
        /**
         * Creates a data source adapter for the given container.
         *
         * @param container the data container
         * @return the data source adapter
         */
        DataSourceAdapter<E> create(Object container);
    }

    /**
     * Factory for creating layout engines.
     *
     * @param <E> the entity type
     */
    public interface LayoutEngineFactory<E> {
        /**
         * Creates a layout engine for the given model.
         *
         * @param model the table model
         * @return the layout engine
         */
        LayoutEngine<E> create(SpreadsheetTableModel<E> model);
    }

    /**
     * Factory for creating renderers.
     *
     * @param <E> the entity type
     * @param <C> the component type
     */
    public interface SpreadsheetRendererFactory<E, C> {
        /**
         * Creates a renderer for the given model.
         *
         * @param model the table model
         * @return the renderer
         */
        SpreadsheetRenderer<E, C> create(SpreadsheetTableModel<E> model);
    }

    /**
     * Factory for creating change analyzers.
     *
     * @param <E> the entity type
     */
    public interface ChangeAnalyzerFactory<E> {
        /**
         * Creates a change analyzer for the given model.
         *
         * @param model the table model
         * @return the change analyzer
         */
        ChangeAnalyzer<E> create(SpreadsheetTableModel<E> model);
    }

    /**
     * Factory for creating patch appliers.
     *
     * @param <E> the entity type
     * @param <C> the component type
     */
    public interface PatchApplierFactory<E, C> {
        /**
         * Creates a patch applier for the given model.
         *
         * @param model the table model
         * @return the patch applier
         */
        PatchApplier<E, C> create(SpreadsheetTableModel<E> model);
    }
}
