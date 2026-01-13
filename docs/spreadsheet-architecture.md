# Goal

Implement a Jmix FlowUI component that adapts Vaadin Spreadsheet to behave like DataGrid, with declarative configuration, pivot support, DataComponents integration, editing, and incremental updates.

---

## Constraints

* Spreadsheet is coordinate-based (cells, rows, columns)
* Jmix uses DataComponents (CollectionContainer)
* Uses DataManager for entity persistence and Metadata for entity creation
* Must avoid full rerender; compute minimal patches (incremental updates implemented)
* Pivot edits may affect multiple source rows

---

## High-Level Architecture

```
StandardListView
  └─ SpreadsheetTableModel (declarative)
        ├─ Columns | Grouping | Filter | Sort
        ├─ Pivot (optional)
        └─ Editing rules
              ↓
      SpreadsheetController
        ├─ DataSourceAdapter (CollectionContainer)
        ├─ LayoutEngine (Flat | Pivot)
        ├─ LayoutIndex (entity↔cells)
        ├─ Renderer (initial render)
        ├─ ChangeAnalyzer (diff)
        └─ PatchApplier (incremental update)
              ↓
        Vaadin Spreadsheet
```

---

## Public API (used by View)

### SpreadsheetTableModel<E>

* entityClass
* columns: List<SpreadsheetColumn<E>>
* grouping
* filter (Condition-based)
* pivot: Optional<SpreadsheetPivot<E>>
* interactionHandler

### SpreadsheetController<E, DC>

* bind(model, CollectionContainer<E>)
* reload()
* getComponent(): Spreadsheet
* save() - saves entities using DataManager
* updateAffectedCells(affectedEntityKeys) - incrementally updates only affected cells

---

## Column Model

```
SpreadsheetColumn<E>
- id
- header
- valueProvider: Function<E, Object>
- setter: BiConsumer<E, Object> (optional)
- formatter
- width, alignment
- editable
```

---

## Pivot Model

### SpreadsheetPivot<E>

* rowAxes: List<PivotAxis<E>>
* columnAxes: List<PivotAxis<E>>
* measures: List<PivotMeasure<E>>
* editStrategy: PivotEditStrategy<E>
* getRowCompletion(): Optional<Supplier<List<Object>>> - provides complete set of row axis values
* getColumnCompletion(): Optional<Supplier<List<Object>>> - provides complete set of column axis values

### PivotAxis<E>

* id
* keyProvider
* comparator
* renderMode (HEADER | MERGED | OUTLINE)

### PivotMeasure<E>

* id
* caption
* valueProvider
* aggregation (SUM | COUNT | AVG | CUSTOM)

---

## Data Integration

### DataSourceAdapter

Wraps CollectionContainer<E>

* subscribes to item/property changes
* emits domain-level change events (entityAdded, entityRemoved, entityChanged, refresh)

### Row Identity

* EntityKeyProvider<E> (default: EntityValues::getId)

### Entity Management

The controller uses:

* **DataManager**: For entity persistence (saveAll())
* **Metadata**: For entity creation (Metadata.create())
* **EntityAdapter**: Abstraction in cell editors for entity operations (create, merge, setProperty)

Entities are saved via DataManager.saveAll(), which returns an EntitySet with saved instances (including new IDs for created entities). The controller updates the CollectionContainer with these saved entities to ensure consistency.

---

## Layout Engine

### LayoutEngine<E>

Interface for building spreadsheet layouts from entities.

### PivotLayoutBuilder<E>

* builds pivot trees (rows, columns) from entities
* supports row/column completion via Supplier<List<Object>> (getRowCompletion(), getColumnCompletion())
* calculates measures (SUM, COUNT, AVG, CUSTOM)
* produces merged regions for headers
* builds measure headers

### FlatTableLayoutBuilder<E>

* Currently a placeholder (not implemented)
* Would map rows×columns → cells for flat table layout

### Output: SpreadsheetLayout

* rows, columns
* merged regions
* CellBinding list

```
CellBinding
- rowIndex
- columnIndex
- value
- style
- entityRef | pivotContext
```

---

## Layout Index (critical for diff)

```
LayoutIndex
- entityKey → Set<CellRef>
- cellRef → CellBinding
- pivotCell → Set<entityKey>
```

Used for:

* click handling
* incremental updates
* pivot recalculation

---

## Editing Flow

### Flat Table

1. User edits cell
2. CellBinding → (entity, setter) via LayoutIndex
3. Update entity property (setter.accept(entity, newValue))
4. Controller saves entities via DataManager.saveAll()
5. Controller updates container with saved entities
6. Controller applies incremental update via updateAffectedCells()

### Pivot

1. User edits pivot cell
2. PivotTableCellEditor extracts pivot context (row/column axes, measure)
3. PivotEditStrategy determines:

   * which entities to modify
   * how to distribute value
4. Entities updated (new entities created via Metadata.create())
5. Controller saves entities via DataManager.saveAll()
6. Controller updates container with saved entities
7. Controller applies incremental update via updateAffectedCells()
8. Layout rebuilt, affected cells updated incrementally

---

## Incremental Updates (Implemented)

### updateAffectedCells()

The controller provides incremental update functionality:

1. Rebuilds layout with fresh entity values
2. Builds new LayoutIndex from rebuilt layout
3. Finds all cells connected to affected entities via LayoutIndex.getCellRefs()
4. Creates LayoutDelta with cells to update
5. Applies incremental update (via PatchApplier or direct cell updates)

**Note**: During cell edit operations, the controller sets a `processingCellEdit` flag to suppress data change listeners (onEntityAdded, onEntityChanged), preventing duplicate updates. The flag is cleared after the incremental update completes.

### ChangeAnalyzer

Input:

* oldEntity (snapshot)
* newEntity
* LayoutIndex

Detects:

* changed properties
* affected columns / measures
* affected pivot cells

(Currently used for validation, not directly for incremental updates)

### LayoutDelta

```
- cellsToUpdate (Set<CellRef>)
- cellsToClear (optional)
- rowsToInsert (rare, not implemented)
- rowsToRemove (rare, not implemented)
```

### PatchApplier

* Currently a placeholder implementation (returns false)
* Falls back to direct cell updates via applyDirectCellUpdates()

### Direct Cell Updates (Current Implementation)

* Updates only changed cells using renderer's cell renderer
* Applies value + style to each affected cell
* Vaadin Spreadsheet's createCell() updates cells directly
* No refreshAllCellValues() call (avoids full refresh)

Fallback to full rerender (reload()) only if:

* LayoutValidator determines structure change requires full rerender
* No cells found to update for affected entities
* Missing dependencies (layoutIndex, changeAnalyzer, currentLayout)

---

## Interaction Layer

* cell → entity mapping via LayoutIndex
* supports click, double-click, selection
* exposes SpreadsheetInteractionHandler<E>

---

## Persistence of State

SpreadsheetState

* column order / width
* sort state
* filter state
* pivot expand/collapse

Stored via UserSettingsTools

---

## Package Structure

```
spreadsheet/
 ├─ api/
 │   ├─ SpreadsheetController
 │   ├─ SpreadsheetTableModel
 │   ├─ SpreadsheetPivot
 │   ├─ PivotEditStrategy
 │   ├─ PivotAxis
 │   ├─ PivotMeasure
 │   └─ SpreadsheetColumn
 ├─ datasource/
 │   ├─ DataSourceAdapter
 │   └─ ContainerSpreadsheetDataSource
 ├─ layout/
 │   ├─ LayoutEngine
 │   ├─ PivotLayoutBuilder
 │   ├─ SpreadsheetLayout
 │   ├─ CellBinding
 │   ├─ PivotCellBinding
 │   └─ MergedRegion
 ├─ index/
 │   ├─ LayoutIndex
 │   └─ DefaultLayoutIndex
 ├─ render/
 │   ├─ SpreadsheetRenderer
 │   └─ DefaultSpreadsheetRenderer
 ├─ edit/
 │   ├─ FlatTableCellEditor
 │   └─ PivotTableCellEditor
 ├─ diff/
 │   ├─ ChangeAnalyzer
 │   ├─ DefaultChangeAnalyzer
 │   ├─ LayoutDelta
 │   ├─ DefaultLayoutDelta
 │   ├─ PatchApplier
 │   ├─ LayoutValidator
 │   └─ LayoutValidationResult
 ├─ ui/
 │   ├─ SpreadsheetControllerFactory
 │   ├─ component/
 │   │   └─ SpreadsheetComponent
 │   ├─ loader/
 │   │   └─ SpreadsheetComponentLoader
 │   └─ config/
 │       └─ SpreadsheetComponentRegistration
 └─ internal/
     └─ DefaultSpreadsheetController
```

---

## Cursor Usage Hint

Use this document as the system context. Ask Cursor to:

* generate skeleton classes per package
* implement ChangeAnalyzer incrementally
* add unit tests for pivot delta logic
* refactor APIs only through api/ package
