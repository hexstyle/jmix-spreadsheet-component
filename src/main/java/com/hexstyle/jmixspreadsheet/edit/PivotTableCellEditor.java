package com.hexstyle.jmixspreadsheet.edit;

import com.hexstyle.jmixspreadsheet.api.PivotAxis;
import com.hexstyle.jmixspreadsheet.api.PivotEditStrategy;
import com.hexstyle.jmixspreadsheet.api.PivotMeasure;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetPivot;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex.CellRef;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.PivotCellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;

import java.util.*;

/**
 * Handles cell editing for pivot tables.
 * <p>
 * This editor processes cell edit events for pivot cells and updates entities
 * using PivotEditStrategy and Metadata. It supports multi-entity updates
 * where a single pivot cell edit can modify multiple source entities.
 * <p>
 * The editing flow:
 * 1. User edits pivot cell
 * 2. PivotEditStrategy determines which entities to modify and how to distribute the value
 * 3. Update entities (created via Metadata, saved via DataManager)
 * 4. Affected pivot cells recalculated (handled by change detection)
 *
 * @param <E> the entity type
 */
public class PivotTableCellEditor<E> {
    
    // Store last edits for tracking affected entities
    private Map<E, Map<String, Object>> lastEdits = null;

    private final SpreadsheetTableModel<E> model;
    private final LayoutIndex<E> layoutIndex;
    private final EntityAdapter<E> entityAdapter;
    private SpreadsheetLayout<E> currentLayout; // Current layout for accessing all bindings

    /**
     * Creates a new pivot cell editor.
     *
     * @param model the table model defining the pivot configuration
     * @param layoutIndex the layout index for mapping cells to bindings
     * @param entityAdapter adapter for entity operations (create, merge, setProperty)
     */
    public PivotTableCellEditor(
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
        this.currentLayout = null; // Will be set by updateLayout() when layout is rebuilt
    }
    
    /**
     * Updates the current layout reference.
     * <p>
     * This method should be called whenever the layout is rebuilt so that the editor
     * has access to all cell bindings, including merged header cells.
     *
     * @param layout the current spreadsheet layout
     */
    public void updateLayout(SpreadsheetLayout<E> layout) {
        this.currentLayout = layout;
    }

    /**
     * Handles a pivot cell edit event.
     * <p>
     * This method updates the entities that contribute to the edited pivot cell
     * using the PivotEditStrategy to determine which entities to modify and how
     * to distribute the new value among them.
     *
     * @param rowIndex the row index of the edited cell (0-based)
     * @param columnIndex the column index of the edited cell (0-based)
     * @param newValue the new cell value
     * @throws IllegalArgumentException if the cell is not editable or not found
     * @throws IllegalStateException if the cell is not a pivot cell or no edit strategy is configured
     */
    public void handleCellEdit(int rowIndex, int columnIndex, Object newValue) {
        // Get pivot configuration
        SpreadsheetPivot<E> pivot = model.getPivot()
                .orElseThrow(() -> new IllegalStateException("Model does not have a pivot configuration"));

        // Get edit strategy
        PivotEditStrategy<E> editStrategy = pivot.getEditStrategy();
        if (editStrategy == null) {
            throw new IllegalStateException("Pivot cells are read-only (no edit strategy configured)");
        }

        // Create cell reference
        CellRef cellRef = createCellRef(rowIndex, columnIndex);

        // Get cell binding from layout index
        CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
        if (binding == null) {
            // Cell not found - might be a regular column cell or not yet indexed
            // This shouldn't happen if DefaultSpreadsheetController properly routes,
            // but handle gracefully
            throw new IllegalArgumentException("Cell not found in layout index: row=" + rowIndex + ", column=" + columnIndex + 
                    ". This may indicate the cell is not a pivot cell or the layout index is out of sync.");
        }

        // Check if this is a pivot cell
        if (!(binding instanceof PivotCellBinding)) {
            throw new IllegalStateException("Cell is not a pivot cell: row=" + rowIndex + ", column=" + columnIndex + 
                    ". Binding type: " + binding.getClass().getName());
        }

        PivotCellBinding<E> pivotBinding = (PivotCellBinding<E>) binding;

        // Get contributing entities
        List<E> contributingEntities = pivotBinding.getSourceEntities();

        // If contributing entities are empty (empty pivot cell), create a new entity
        if (contributingEntities.isEmpty()) {
            // Extract axis values and measure ID from cell position first
            PivotContextInfo contextInfo = extractPivotContext(rowIndex, columnIndex, pivot);
            
            // Create a new entity (but don't merge it yet - merge after properties are set)
            E newEntity = createEntityForEmptyPivotCell(rowIndex, columnIndex, pivot, newValue);
            if (newEntity != null) {
                // Create pivot edit context with the newly created entity (not yet merged)
                PivotEditContextImpl<E> pivotContext = new PivotEditContextImpl<>(
                        Collections.singletonList(newEntity),
                        contextInfo.rowAxisValues,
                        contextInfo.columnAxisValues,
                        contextInfo.measureId,
                        null // No current value for new entity
                );
                
                // Determine edits using the strategy
                // The strategy will set properties on the entity via propertyUpdates
                Map<E, Map<String, Object>> edits = editStrategy.determineEdits(pivotContext, newValue);
                
                // Store edits for tracking affected entities (used by controller)
                this.lastEdits = edits;
                
                // Apply edits - this will merge the entity and set properties
                applyEdits(edits);
                
                // DataContext will automatically track the new entity
                return;
            }
        }

        // Extract axis values and measure ID from cell position
        PivotContextInfo contextInfo = extractPivotContext(rowIndex, columnIndex, pivot);

        // Create pivot edit context
        PivotEditContextImpl<E> pivotContext = new PivotEditContextImpl<>(
                contributingEntities,
                contextInfo.rowAxisValues,
                contextInfo.columnAxisValues,
                contextInfo.measureId,
                pivotBinding.getValue()
        );

        // Determine edits using the strategy
        // Strategy can return new entities (for empty pivot cells) or modify existing ones
        Map<E, Map<String, Object>> edits = editStrategy.determineEdits(pivotContext, newValue);

        // Store edits for tracking affected entities (used by controller)
        this.lastEdits = edits;

        // Apply edits to entities via DataContext
        // This will create new entities if they don't exist yet
        applyEdits(edits);

        // DataContext will automatically track the changes, and DataContainer will emit events
    }
    
    /**
     * Returns the entities that were affected by the last edit operation.
     * This is used by the controller to track which entities need incremental updates.
     *
     * @return the set of entities that were modified in the last edit
     */
    public java.util.Set<E> getLastAffectedEntities() {
        if (lastEdits == null || lastEdits.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        return new java.util.HashSet<>(lastEdits.keySet());
    }

    /**
     * Handles a pivot cell edit event with a pre-validated binding.
     * <p>
     * This overload accepts the binding directly to avoid re-lookup in the layout index.
     *
     * @param rowIndex the row index of the edited cell (0-based)
     * @param columnIndex the column index of the edited cell (0-based)
     * @param newValue the new cell value
     * @param pivotBinding the pre-validated pivot cell binding
     */
    public void handleCellEdit(int rowIndex, int columnIndex, Object newValue, PivotCellBinding<E> pivotBinding) {
        // Get pivot configuration
        SpreadsheetPivot<E> pivot = model.getPivot()
                .orElseThrow(() -> new IllegalStateException("Model does not have a pivot configuration"));

        // Get edit strategy
        PivotEditStrategy<E> editStrategy = pivot.getEditStrategy();
        if (editStrategy == null) {
            throw new IllegalStateException("Pivot cells are read-only (no edit strategy configured)");
        }

        // Get contributing entities
        List<E> contributingEntities = pivotBinding.getSourceEntities();

        // If contributing entities are empty (empty pivot cell), create a new entity
        if (contributingEntities.isEmpty()) {
            // Extract axis values and measure ID from cell position first
            PivotContextInfo contextInfo = extractPivotContext(rowIndex, columnIndex, pivot);
            
            // Create a new entity (but don't merge it yet - merge after properties are set)
            E newEntity = createEntityForEmptyPivotCell(rowIndex, columnIndex, pivot, newValue);
            if (newEntity != null) {
                // Create pivot edit context with the newly created entity (not yet merged)
                PivotEditContextImpl<E> pivotContext = new PivotEditContextImpl<>(
                        Collections.singletonList(newEntity),
                        contextInfo.rowAxisValues,
                        contextInfo.columnAxisValues,
                        contextInfo.measureId,
                        null // No current value for new entity
                );
                
                // Determine edits using the strategy
                // The strategy will set properties on the entity via propertyUpdates
                Map<E, Map<String, Object>> edits = editStrategy.determineEdits(pivotContext, newValue);
                
                // Store edits for tracking affected entities (used by controller)
                this.lastEdits = edits;
                
                // Apply edits - this will merge the entity and set properties
                applyEdits(edits);
                
                // DataContext will automatically track the new entity
                return;
            }
        }

        // Extract axis values and measure ID from cell position
        PivotContextInfo contextInfo = extractPivotContext(rowIndex, columnIndex, pivot);

        // Create pivot edit context
        PivotEditContextImpl<E> pivotContext = new PivotEditContextImpl<>(
                contributingEntities,
                contextInfo.rowAxisValues,
                contextInfo.columnAxisValues,
                contextInfo.measureId,
                pivotBinding.getValue()
        );

        // Determine edits using the strategy
        // Strategy can return new entities (for empty pivot cells) or modify existing ones
        Map<E, Map<String, Object>> edits = editStrategy.determineEdits(pivotContext, newValue);

        // Store edits for tracking affected entities (used by controller)
        this.lastEdits = edits;

        // Apply edits to entities via DataContext
        // This will create new entities if they don't exist yet
        applyEdits(edits);

        // DataContext will automatically track the changes, and DataContainer will emit events
    }

    /**
     * Creates a new entity for an empty pivot cell.
     * <p>
     * This method uses DataContext.create() to create a new entity instance,
     * following Jmix UI best practices. Never use 'new Entity()' - always use
     * DataContext.create().
     * <p>
     * The PivotEditStrategy is responsible for setting all properties based on
     * the pivot context (row axis values, column axis values, etc.).
     *
     * @param rowIndex the row index of the edited cell
     * @param columnIndex the column index of the edited cell
     * @param pivot the pivot configuration
     * @param newValue the new cell value
     * @return a new managed entity instance, or null if creation is not supported
     */
    private E createEntityForEmptyPivotCell(int rowIndex, int columnIndex, 
                                            SpreadsheetPivot<E> pivot, Object newValue) {
        // Get the entity class from the model
        Class<E> entityClass = model.getEntityClass();
        
        // Use Metadata.create() to create a new entity instance
        try {
            E entity = entityAdapter.create(entityClass);
            // The PivotEditStrategy will handle setting all properties based on pivot context
            return entity;
        } catch (Exception e) {
            // Cannot create entity - strategy should handle this
            return null;
        }
    }

    /**
     * Extracts pivot context information from a cell position.
     * <p>
     * This method determines the row axis values, column axis values, and measure ID
     * for a given cell position in the pivot table.
     *
     * @param rowIndex the row index
     * @param columnIndex the column index
     * @param pivot the pivot configuration
     * @return the pivot context information
     */
    private PivotContextInfo extractPivotContext(int rowIndex, int columnIndex, SpreadsheetPivot<E> pivot) {
        // Calculate header dimensions
        int rowHeaderCols = pivot.getRowAxes().size();
        int colHeaderRows = pivot.getColumnAxes().size();
        
        // Calculate where headers actually start (after regular columns and row headers)
        int regularColCount = model.getColumns() != null ? model.getColumns().size() : 0;
        int headerStartCol = regularColCount + rowHeaderCols;

        // Extract row axis values from header cells
        Map<String, Object> rowAxisValues = new LinkedHashMap<>();
        
        // Extract row axis values from row header cells (when row axes are defined)
        if (!pivot.getRowAxes().isEmpty() && rowIndex >= colHeaderRows) {
            List<PivotAxis<E>> rowAxes = pivot.getRowAxes();
            // Row headers are in columns starting from regularColCount
            for (int axisLevel = 0; axisLevel < rowAxes.size(); axisLevel++) {
                PivotAxis<E> axis = rowAxes.get(axisLevel);
                String axisId = axis.getId();
                int headerCol = regularColCount + axisLevel;
                
                // Find the row header cell for this axis level in this row
                CellRef cellRef = createCellRef(rowIndex, headerCol);
                CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
                
                // Fallback: search layout directly if not found in layout index
                if (binding == null && currentLayout != null) {
                    for (CellBinding<E> layoutBinding : currentLayout.getCellBindings()) {
                        if (layoutBinding.getRowIndex() == rowIndex && 
                            layoutBinding.getColumnIndex() == headerCol) {
                            binding = layoutBinding;
                            break;
                        }
                    }
                }
                
                if (binding != null && binding.getValue() != null) {
                    // Get the value from the header cell
                    // The value might be a formatted string (e.g., date string), which the strategy will parse
                    Object value = binding.getValue();
                    rowAxisValues.put(axisId, value);
                }
            }
        }
        
        // Extract row axis values from regular columns (if no row axes are defined)
        // This allows strategies to access values from regular columns (e.g., Day column)
        // The strategy is responsible for interpreting these values for the specific entity type
        if (pivot.getRowAxes().isEmpty() && rowIndex >= colHeaderRows) {
            // Extract values from regular columns in the same row
            // Use column IDs from the model to identify which values to extract
            List<SpreadsheetColumn<E>> regularColumns = model.getColumns();
            if (regularColumns != null) {
                for (int colIndex = 0; colIndex < regularColumns.size() && colIndex < headerStartCol; colIndex++) {
                    SpreadsheetColumn<E> column = regularColumns.get(colIndex);
                    String columnId = column.getId();
                    
                    CellRef cellRef = createCellRef(rowIndex, colIndex);
                    CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
                    if (binding != null) {
                        // Try to get the actual value from entity reference first (for Day column, this is LocalDate)
                        // If entity reference is available, use it to get the raw property value
                        Object value = null;
                        if (binding.getEntityRef() != null && "day".equals(columnId)) {
                            // For Day column, try to get the actual LocalDate from the entity
                            E entity = binding.getEntityRef();
                            try {
                                value = io.jmix.core.entity.EntityValues.getValue(entity, "day");
                            } catch (Exception e) {
                                // Fallback to cell value
                                value = binding.getValue();
                            }
                        } else if ("day".equals(columnId)) {
                            // For Day column, even if no entity reference, try to use cell value
                            // The value might be a formatted string that the strategy can parse
                            value = binding.getValue();
                        } else {
                            // For other columns, use the cell value
                            value = binding.getValue();
                        }
                        
                        if (value != null) {
                            // Store the raw cell value - strategy will interpret it
                            rowAxisValues.put(columnId, value);
                        }
                    } else if ("day".equals(columnId)) {
                        // Day column binding not found - try to find it in currentLayout if available
                        if (currentLayout != null) {
                            for (CellBinding<E> layoutBinding : currentLayout.getCellBindings()) {
                                if (layoutBinding.getRowIndex() == rowIndex && 
                                    layoutBinding.getColumnIndex() == colIndex) {
                                    // Found the binding in layout - try to get value
                                    Object value = null;
                                    if (layoutBinding.getEntityRef() != null) {
                                        try {
                                            value = io.jmix.core.entity.EntityValues.getValue(
                                                    layoutBinding.getEntityRef(), "day");
                                        } catch (Exception e) {
                                            value = layoutBinding.getValue();
                                        }
                                    } else {
                                        value = layoutBinding.getValue();
                                    }
                                    if (value != null) {
                                        rowAxisValues.put(columnId, value);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Extract column axis values and measure ID from column position
        Map<String, Object> columnAxisValues = new LinkedHashMap<>();
        String measureId = null;

        // Check if this is a data cell (not a header cell)
        // Data cells start after regular columns, row headers, and column headers
        // When there are no pivot columns (colHeaderRows = 0), data cells start at row 0
        if (columnIndex >= headerStartCol && rowIndex >= colHeaderRows) {
            // This is a data cell - extract column axis values and measure ID
            List<PivotAxis<E>> columnAxes = pivot.getColumnAxes();
            List<PivotMeasure<E>> measures = pivot.getMeasures();
            
            // Extract column axis values from column header cells (only if pivot columns exist)
            if (!columnAxes.isEmpty() && colHeaderRows > 0) {
                // Look at column header cells to extract axis values
                // Headers are in rows 0 to (colHeaderRows - 1)
                // Headers may be merged, so we need to find the header cell that covers columnIndex
                for (int headerRow = 0; headerRow < colHeaderRows && headerRow < columnAxes.size(); headerRow++) {
                    // Search backwards from columnIndex to find the header cell that covers this column
                    // Headers are created left-to-right, so find the rightmost header that starts before or at columnIndex
                    CellBinding<E> headerBinding = findHeaderCellInRow(headerRow, columnIndex);
                    if (headerBinding != null && headerBinding.getValue() != null) {
                        String headerValue = headerBinding.getValue().toString();
                        
                        // Map header value to axis ID
                        PivotAxis<E> axis = columnAxes.get(headerRow);
                        String axisId = axis.getId();
                        
                        // Store the raw header value - the strategy is responsible for
                        // converting it to the appropriate entity type if needed
                        columnAxisValues.put(axisId, headerValue);
                    }
                }
            }
            
            // Determine which measure this cell represents (only if measures exist)
            if (!measures.isEmpty()) {
                // Calculate data column offset (columns after regular columns and row headers)
                int dataCol = columnIndex - headerStartCol;
                
                // If there are pivot columns, calculate measure index based on pivot column structure
                // Otherwise, measures are applied directly (one measure per column after regular columns)
                if (!columnAxes.isEmpty() && dataCol >= 0) {
                    // Calculate which measure this cell represents based on pivot column structure
                    // This assumes columns are organized as: regularCols + rowHeaderCols + (pivotCols * measures)
                    int measureCount = measures.size();
                    if (measureCount > 0) {
                        int measureIndex = dataCol % measureCount;
                        if (measureIndex < measures.size()) {
                            measureId = measures.get(measureIndex).getId();
                        }
                    }
                } else if (dataCol >= 0) {
                    // No pivot columns - measures are applied directly
                    // Each column after regular columns + row headers represents a measure
                    int measureIndex = dataCol;
                    if (measureIndex < measures.size()) {
                        measureId = measures.get(measureIndex).getId();
                    }
                }
            }
        }

        return new PivotContextInfo(rowAxisValues, columnAxisValues, measureId);
    }

    /**
     * Finds the header cell in a specific row that covers the given column index.
     * <p>
     * Since header cells can be merged, this method first tries to find all header cells
     * in the row from the layout, then determines which one covers the target column.
     * If layout is not available, it falls back to searching backwards.
     *
     * @param headerRow the row index of the header
     * @param targetColumn the target column index
     * @return the header cell binding that covers the target column, or null if not found
     */
    private CellBinding<E> findHeaderCellInRow(int headerRow, int targetColumn) {
        // Headers start after regular columns and row headers
        // Calculate where headers actually start
        int rowHeaderCols = model.getPivot()
                .map(p -> p.getRowAxes().size())
                .orElse(0);
        
        // Get regular column count from model
        int regularColCount = model.getColumns() != null ? model.getColumns().size() : 0;
        int headerStartCol = regularColCount + rowHeaderCols;
        
        // Headers can't be before their start position
        if (targetColumn < headerStartCol) {
            return null;
        }
        
        // First, try to find header from layout if available (most reliable for merged headers)
        if (currentLayout != null) {
            CellBinding<E> headerFromLayout = findHeaderFromLayout(headerRow, targetColumn, headerStartCol);
            if (headerFromLayout != null) {
                return headerFromLayout;
            }
        }
        
        // Fallback: search backwards from targetColumn using layout index
        // First, try exact match
        CellRef exactCellRef = createCellRef(headerRow, targetColumn);
        CellBinding<E> exactBinding = layoutIndex.getCellBinding(exactCellRef);
        if (exactBinding != null && exactBinding.getValue() != null && isHeaderCell(exactBinding)) {
            return exactBinding;
        }
        
        // Search backwards from targetColumn to find the header cell that covers it
        // Headers are created left-to-right, so find the rightmost header that starts before or at targetColumn
        int searchStart = Math.max(headerStartCol, targetColumn - 200); // Search up to 200 columns back
        
        // Track the rightmost header we find (closest to targetColumn)
        CellBinding<E> bestMatch = null;
        int bestMatchCol = -1;
        
        for (int col = targetColumn; col >= searchStart; col--) {
            CellRef cellRef = createCellRef(headerRow, col);
            CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
            if (binding != null && binding.getValue() != null && isHeaderCell(binding)) {
                // Found a header cell - this is likely the one that covers targetColumn
                // Since we're searching backwards, the first one we find (rightmost) is the best match
                // Headers are merged left-to-right, so if we find one at col <= targetColumn,
                // it likely covers targetColumn (if merged)
                if (bestMatch == null || col > bestMatchCol) {
                    bestMatch = binding;
                    bestMatchCol = col;
                }
                // If we found a header at or very close to targetColumn, use it immediately
                if (col >= targetColumn - 5) {
                    return binding;
                }
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Finds header cell from layout by iterating through all bindings in the header row.
     * <p>
     * This method finds all header cells in the row and determines which one covers
     * the target column based on merged regions.
     *
     * @param headerRow the row index of the header
     * @param targetColumn the target column index
     * @param headerStartCol the column where headers start
     * @return the header cell binding that covers the target column, or null if not found
     */
    private CellBinding<E> findHeaderFromLayout(int headerRow, int targetColumn, int headerStartCol) {
        // Collect all headers in this row
        List<CellBinding<E>> headersInRow = new ArrayList<>();
        for (CellBinding<E> binding : currentLayout.getCellBindings()) {
            if (binding.getRowIndex() == headerRow && isHeaderCell(binding)) {
                int bindingCol = binding.getColumnIndex();
                if (bindingCol >= headerStartCol) {
                    headersInRow.add(binding);
                }
            }
        }
        
        if (headersInRow.isEmpty()) {
            return null;
        }
        
        // Sort headers by column index
        headersInRow.sort(Comparator.comparingInt(CellBinding::getColumnIndex));
        
        // Find the rightmost header that starts before or at targetColumn
        // and check merged regions to see if it covers targetColumn
        CellBinding<E> bestMatch = null;
        int bestMatchCol = -1;
        
        for (CellBinding<E> binding : headersInRow) {
            int bindingCol = binding.getColumnIndex();
            
            if (bindingCol > targetColumn) {
                // Header starts after target column - can't cover it
                break;
            }
            
            // Check if this header covers targetColumn via merged region
            boolean coversTarget = false;
            if (bindingCol == targetColumn) {
                // Exact match
                coversTarget = true;
            } else {
                // Check merged regions to see if this header's merged region covers targetColumn
                for (MergedRegion mergedRegion : currentLayout.getMergedRegions()) {
                    if (mergedRegion.getFirstRow() == headerRow && 
                        mergedRegion.getLastRow() == headerRow &&
                        mergedRegion.getFirstColumn() == bindingCol &&
                        mergedRegion.getLastColumn() >= targetColumn &&
                        mergedRegion.getFirstColumn() <= targetColumn) {
                        // This merged region covers the target column
                        coversTarget = true;
                        break;
                    }
                }
                
                // If no merged region found, check if there's another header between this one and target
                // If not, assume this header covers targetColumn (heuristic for merged headers without explicit region)
                if (!coversTarget) {
                    boolean hasHeaderBetween = false;
                    for (CellBinding<E> otherBinding : headersInRow) {
                        int otherCol = otherBinding.getColumnIndex();
                        if (otherCol > bindingCol && otherCol <= targetColumn) {
                            hasHeaderBetween = true;
                            break;
                        }
                    }
                    // If no header between, this header likely covers targetColumn (if merged)
                    if (!hasHeaderBetween) {
                        coversTarget = true;
                    }
                }
            }
            
            if (coversTarget && (bestMatch == null || bindingCol > bestMatchCol)) {
                bestMatch = binding;
                bestMatchCol = bindingCol;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Checks if a cell binding is a header cell.
     *
     * @param binding the cell binding to check
     * @return true if the binding is a header cell, false otherwise
     */
    private boolean isHeaderCell(CellBinding<E> binding) {
        if (binding == null || binding.getValue() == null) {
            return false;
        }
        
        // Header cells are PivotCellBindings with empty source entities
        if (binding instanceof PivotCellBinding) {
            PivotCellBinding<E> pivotBinding = (PivotCellBinding<E>) binding;
            return pivotBinding.getSourceEntities().isEmpty();
        }
        
        // Non-pivot bindings at header rows without entity ref are also headers (e.g., regular column headers)
        return binding.getEntityRef() == null && binding.getPivotContext() == null;
    }

    /**
     * Applies edits to a managed entity directly without merging again.
     * <p>
     * This method is used when the entity is already merged into DataContext
     * (e.g., when creating a new entity for an empty pivot cell).
     * It applies property updates directly to the managed entity.
     *
     * @param managedEntity the entity that is already managed in DataContext
     * @param edits the map of entities to their property updates
     */
    private void applyEditsToManagedEntity(E managedEntity, Map<E, Map<String, Object>> edits) {
        if (edits == null || edits.isEmpty()) {
            return;
        }

        // Find the property updates for the managed entity
        // The edits map may contain the managed entity as a key
        Map<String, Object> propertyUpdates = edits.get(managedEntity);
        
        // If not found, try to find by identity (for IdentityHashMap)
        if (propertyUpdates == null) {
            for (Map.Entry<E, Map<String, Object>> entry : edits.entrySet()) {
                if (entry.getKey() == managedEntity) {
                    propertyUpdates = entry.getValue();
                    break;
                }
            }
        }

        if (propertyUpdates != null && !propertyUpdates.isEmpty()) {
            // Apply property updates directly to the managed entity
            applyPropertyUpdates(managedEntity, propertyUpdates);
        }
    }

    /**
     * Applies edits to entities via DataContext.
     * <p>
     * This method merges entities into DataContext and applies property updates
     * for each entity according to the edits map.
     * <p>
     * Note: This method should not be used for entities that are already managed.
     * Use {@link #applyEditsToManagedEntity(Object, Map)} instead for managed entities.
     *
     * @param edits the map of entities to their property updates
     */
    private void applyEdits(Map<E, Map<String, Object>> edits) {
        if (edits == null || edits.isEmpty()) {
            return;
        }

        // Process edits in a way that avoids hashCode() issues with entities without IDs
        for (Map.Entry<E, Map<String, Object>> entry : edits.entrySet()) {
            E entity = entry.getKey();
            Map<String, Object> propertyUpdates = entry.getValue();

            if (propertyUpdates == null || propertyUpdates.isEmpty()) {
                continue;
            }

            // Check if entity is new (no ID)
            // Entities created with DataContext.create() are already managed and don't need merge
            // Entities created with 'new Entity()' need to be merged
            Object entityId = io.jmix.core.entity.EntityValues.getId(entity);
            E managedEntity;
            
            if (entityId == null) {
                // New entity - check if it was created with DataContext.create() or with 'new'
                // Entities created with DataContext.create() are already managed and tracked
                // Entities created with 'new' need to be merged first
                
                // For entities created with DataContext.create(), they're already managed.
                // Set properties first, then merge (merge will just return the same instance).
                // Setting properties first ensures validation doesn't fail if merge triggers lifecycle events.
                applyPropertyUpdates(entity, propertyUpdates);
                
                try {
                    // Merge the entity - for DataContext.create() entities, this just returns the same instance
                    // For 'new' entities, this tracks them in the context
                    managedEntity = entityAdapter.merge(entity);
                } catch (IllegalStateException e) {
                    // If merge fails, the entity might not be properly set up
                    // This shouldn't happen for DataContext.create() entities, but handle gracefully
                    managedEntity = entity;
                }
            } else {
                // Existing entity - merge first, then set properties
                try {
                    managedEntity = entityAdapter.merge(entity);
                    // Apply property updates to the managed entity
                    applyPropertyUpdates(managedEntity, propertyUpdates);
                } catch (Exception e) {
                    // If merge fails (e.g., entity already in context),
                    // try to apply updates directly to the entity
                    applyPropertyUpdates(entity, propertyUpdates);
                    managedEntity = entity;
                }
            }
        }
    }

    /**
     * Applies property updates to an entity using the property setter.
     * <p>
     * This method uses the property setter provided by the data context adapter
     * to set properties by name on the entity.
     *
     * @param entity the entity to update
     * @param propertyUpdates the map of property names to new values
     */
    private void applyPropertyUpdates(E entity, Map<String, Object> propertyUpdates) {
        if (propertyUpdates == null || propertyUpdates.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : propertyUpdates.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Use the property setter to set the property
            // The DataContextAdapter is responsible for any type conversions needed
            entityAdapter.setProperty(entity, propertyName, propertyValue);
        }
    }

    /**
     * Creates a cell reference for the given row and column indices.
     *
     * @param row the row index (0-based)
     * @param column the column index (0-based)
     * @return the cell reference
     */
    private CellRef createCellRef(int row, int column) {
        return new CellRefImpl(row, column);
    }

    /**
     * Implementation of CellRef that properly implements equals/hashCode
     * for use in Map lookups in LayoutIndex.
     */
    private static class CellRefImpl implements CellRef {
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
            if (!(o instanceof CellRef)) return false;
            CellRef that = (CellRef) o;
            return row == that.getRow() && column == that.getColumn();
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, column);
        }
    }

    /**
     * Holds pivot context information extracted from cell position.
     */
    private static class PivotContextInfo {
        final Map<String, Object> rowAxisValues;
        final Map<String, Object> columnAxisValues;
        final String measureId;

        PivotContextInfo(Map<String, Object> rowAxisValues,
                        Map<String, Object> columnAxisValues,
                        String measureId) {
            this.rowAxisValues = rowAxisValues != null ? Collections.unmodifiableMap(rowAxisValues) : Collections.emptyMap();
            this.columnAxisValues = columnAxisValues != null ? Collections.unmodifiableMap(columnAxisValues) : Collections.emptyMap();
            this.measureId = measureId;
        }
    }

    /**
     * Implementation of PivotEditContext.
     */
    private static class PivotEditContextImpl<E> implements PivotEditStrategy.PivotEditContext<E> {
        private final List<E> contributingEntities;
        private final Map<String, Object> rowAxisValues;
        private final Map<String, Object> columnAxisValues;
        private final String measureId;
        private final Object currentValue;

        PivotEditContextImpl(List<E> contributingEntities,
                            Map<String, Object> rowAxisValues,
                            Map<String, Object> columnAxisValues,
                            String measureId,
                            Object currentValue) {
            this.contributingEntities = contributingEntities != null
                    ? Collections.unmodifiableList(contributingEntities)
                    : Collections.emptyList();
            this.rowAxisValues = rowAxisValues != null ? Collections.unmodifiableMap(rowAxisValues) : Collections.emptyMap();
            this.columnAxisValues = columnAxisValues != null ? Collections.unmodifiableMap(columnAxisValues) : Collections.emptyMap();
            this.measureId = measureId;
            this.currentValue = currentValue;
        }

        @Override
        public List<E> getContributingEntities() {
            return contributingEntities;
        }

        @Override
        public Map<String, Object> getRowAxisValues() {
            return rowAxisValues;
        }

        @Override
        public Map<String, Object> getColumnAxisValues() {
            return columnAxisValues;
        }

        @Override
        public String getMeasureId() {
            return measureId;
        }

        @Override
        public Object getCurrentValue() {
            return currentValue;
        }
    }

    /**
     * Adapter interface for entity operations.
     * <p>
     * This abstraction allows the cell editor to work with entities using Metadata and DataManager.
     *
     * @param <E> the entity type
     */
    public interface EntityAdapter<E> {
        /**
         * Creates a new entity instance using Metadata.create().
         *
         * @param entityClass the entity class to create
         * @return a new entity instance
         */
        E create(Class<E> entityClass);

        /**
         * Processes an entity (no-op with DataManager, kept for interface compatibility).
         *
         * @param entity the entity
         * @return the entity (unchanged)
         */
        E merge(E entity);

        /**
         * Sets a property value on an entity by property name.
         * <p>
         * This method sets a property on the entity using its name.
         * The implementation should use Jmix's MetaProperty system or
         * similar reflection-based approach to set the property.
         *
         * @param entity the entity to update
         * @param propertyName the name of the property to set
         * @param propertyValue the new value for the property
         */
        void setProperty(E entity, String propertyName, Object propertyValue);
        
        /**
         * Tracks an entity (no-op with DataManager, kept for interface compatibility).
         *
         * @param entity the entity
         * @return the entity (unchanged)
         */
        default E track(E entity) {
            return entity;
        }
    }

    /**
     * Optional interface for entity lookup operations.
     * <p>
     * Implementations that support entity lookup by name can implement this interface.
     */
    public interface EntityLookup {
        /**
         * Finds an entity by its name.
         *
         * @param entityClass the entity class
         * @param name the name to find
         * @return the entity, or null if not found
         */
        Object findEntityByName(Class<?> entityClass, String name);
    }
}
