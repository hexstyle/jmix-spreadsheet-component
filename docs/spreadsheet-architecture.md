# Goal

Provide a FlowUI component that wraps Vaadin Spreadsheet and behaves like a Jmix DataGrid, with declarative configuration, pivot support, custom layouts, editing, and incremental updates.

---

## Runtime Modes

The component runs in two modes driven by `SpreadsheetComponentConfig.Mode`:

1. TABLE mode
- Uses `SpreadsheetTableModel` and a `CollectionContainer`
- `DefaultSpreadsheetController` builds layouts and handles incremental updates
- Layout engine is `PivotLayoutBuilder` (pivot) or `FlatTableLayoutBuilder` (flat table)

2. LAYOUT mode
- Uses a pre-built `SpreadsheetLayout` from a supplier
- `LayoutSpreadsheetController` renders the layout and handles interaction
- No incremental updates; `updateAffectedCells()` triggers a full reload

---

## Configuration Entry Point

`SpreadsheetComponent.configure(SpreadsheetComponentConfig)` is the primary entry point.
It resolves defaults (from XML or component properties), creates the correct controller, binds it, and attaches it.

Key config fields:
- Mode: `forTable(...)` or `forLayout(...)`
- Interaction handler and cell key provider
- read-only, navigation grid visibility
- auto resize and viewport refresh
- header row index, header style and enablement
- header width overrides and deltas
- style rules and style provider
- post-render hook (`afterRender`)

The component loader reads XML attributes and sets component defaults:
`readOnly`, `navigationGridVisible`, `autoRefreshViewport`, `autoResize`, `headerStyle`.
Config values override these defaults.

---

## Control Flow (TABLE mode)

```
SpreadsheetComponent.configure(config)
  -> SpreadsheetComponentFactory creates Spreadsheet + DefaultSpreadsheetController
  -> controller.bind(model, CollectionContainer)
      -> DataSourceAdapter
      -> LayoutEngine (Flat or Pivot)
      -> SpreadsheetRenderer (PoiSpreadsheetRenderer)
      -> ChangeAnalyzer + PatchApplier + LayoutValidator
      -> performInitialRender
          -> build layout
          -> render
          -> postRenderHook
          -> PoiSpreadsheetRenderer.flush
          -> build LayoutIndex
      -> create editors + attach listeners
```

The postRenderHook is critical. It applies:
- `SpreadsheetRenderSupport.resizeSheet`
- `SpreadsheetRenderSupport.refreshViewport`
- header column widths
- row grouping refresh
- `SpreadsheetComponentConfig.afterRender`

After any render, the POI renderer must be flushed to refresh changed cells.

---

## Control Flow (LAYOUT mode)

```
SpreadsheetComponent.configure(config)
  -> SpreadsheetComponentFactory creates LayoutSpreadsheetController
  -> controller.bind(model, dummyObject)
      -> build layout from supplier
      -> resize (optional)
      -> render
      -> refresh viewport (optional)
      -> header widths + grouping
      -> LayoutIndex build
      -> PoiSpreadsheetRenderer.flush
      -> afterRender
```

---

## Key Components and Responsibilities

Controllers
- `DefaultSpreadsheetController` - table mode, incremental updates, editing, persistence
- `LayoutSpreadsheetController` - layout mode, render only, selection handling

Layout
- `SpreadsheetLayout` - immutable layout model for cells, merges, and row groups
- `DefaultSpreadsheetLayout` - simple immutable implementation
- `FlatTableLayoutBuilder` - builds a header row + data rows and optional row grouping
- `PivotLayoutBuilder` - builds pivot headers, measures, merges, and data cells

Rendering
- `PoiSpreadsheetRenderer` - applies values and CSS-like styles via POI
- `SpreadsheetRenderSupport` - resizes sheet, refreshes viewport, applies header widths, refreshes row grouping

Indexing and Interaction
- `LayoutIndex` and `DefaultLayoutIndex` map entity keys and cell refs
- `SpreadsheetInteractionBridge` translates selection events to `SpreadsheetInteractionHandler`

Styling
- `StyleRule`, `StyleToken`, `CellStyleContext` evaluate conditional styles
- `SpreadsheetComponentOptions` holds rules and style provider for the renderer

---

## Data Integration and Persistence

TABLE mode uses:
- `ContainerSpreadsheetDataSource` to watch `CollectionContainer`
- `DataManager` for persistence (`saveAll`)
- `Metadata` for entity creation (used by editors)

LAYOUT mode does not use a container or persistence by default.

---

## Editing Flow

Flat table:
1. User edits a cell
2. `FlatTableCellEditor` updates the entity via setter
3. `DefaultSpreadsheetController` saves entities via `DataManager.saveAll`
4. `updateAffectedCells` applies incremental updates

Pivot table:
1. User edits a pivot cell
2. `PivotTableCellEditor` computes edits via `PivotEditStrategy`
3. Entities are saved and incremental updates are applied

Read-only is enforced in the controller; non-editable cells are reverted after edits.

---

## Incremental Updates (TABLE mode)

`updateAffectedCells`:
- Rebuilds the layout
- Builds a new `LayoutIndex`
- Computes affected cells via index and `LayoutValidator`
- Attempts patch application (default is no-op)
- Falls back to direct cell updates via the renderer
- Flushes POI renderer updates when needed

If structure changes or no affected cells are found, a full `reload()` is used.

LAYOUT mode always reloads.

---

## Refactoring Guidance

Keep these invariants intact when refactoring:

1. Public API stability
- Classes under `com.hexstyle.jmixspreadsheet.api` and `SpreadsheetComponent` / `SpreadsheetComponentConfig` are the public surface.
- Internal controllers and helpers can move, but the API must stay stable.

2. Configure -> create -> bind chain
- `SpreadsheetComponent.configure` must always create a controller, bind it, and attach the underlying `Spreadsheet`.

3. Render lifecycle
- Render must be followed by postRenderHook and `PoiSpreadsheetRenderer.flush`.
- `LayoutIndex` must be rebuilt after render.

4. Editing and read-only
- Read-only enforcement happens in the controller.
- For pivot edits, `PivotEditStrategy` is required for editability.

5. Layout mode isolation
- `LayoutSpreadsheetController` should remain independent from Jmix DataComponents.

6. XML loader defaults
- Changes to component-level defaults must be mirrored in `SpreadsheetComponentLoader` and `SpreadsheetComponentConfig` resolution.

When changing layout or rendering behavior, review:
- `DefaultSpreadsheetController` and `LayoutSpreadsheetController`
- `SpreadsheetComponentFactory` in `SpreadsheetComponent`
- `SpreadsheetRenderSupport` utilities
