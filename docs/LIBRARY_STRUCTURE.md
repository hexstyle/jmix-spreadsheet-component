# Jmix Spreadsheet Library Structure

This document describes the structure of the Jmix Spreadsheet component library.

## Library Package Root

Reusable spreadsheet code lives under:

```
com.hexstyle.jmixspreadsheet.*
```

## Package Organization

- `com.hexstyle.jmixspreadsheet.api` - Public API interfaces and models
- `com.hexstyle.jmixspreadsheet.datasource` - Data source adapters for Jmix containers
- `com.hexstyle.jmixspreadsheet.layout` - Layout engines and layout model (flat + pivot)
- `com.hexstyle.jmixspreadsheet.index` - LayoutIndex and cell/entity mapping
- `com.hexstyle.jmixspreadsheet.render` - Renderers and SpreadsheetRenderSupport utilities
- `com.hexstyle.jmixspreadsheet.edit` - Cell edit handlers (flat + pivot)
- `com.hexstyle.jmixspreadsheet.diff` - Change analysis, layout deltas, patch appliers
- `com.hexstyle.jmixspreadsheet.ui` - Styling rules, options, controller factory
- `com.hexstyle.jmixspreadsheet.ui.component` - SpreadsheetComponent and SpreadsheetComponentConfig
- `com.hexstyle.jmixspreadsheet.ui.loader` - XML component loader
- `com.hexstyle.jmixspreadsheet.ui.config` - Spring registration
- `com.hexstyle.jmixspreadsheet.internal` - Controllers and interaction bridge (not public API)

## Demo Application Code (Not Part of Library)

The demo application lives under `com.example.scm.*` and includes:

- Entities, repositories, and services
- Views and UI controllers
- Port balance layout and aggregation code

## Resources

- `src/main/resources/META-INF/resources/schemas/spreadsheet.xsd` - XML schema for `<spreadsheet:spreadsheet>`

## Spring Configuration

- `SpreadsheetComponentRegistration` registers the component and schema
- `SpreadsheetComponentLoader` loads XML attributes into SpreadsheetComponent
- `SpreadsheetControllerFactory` provides legacy manual controller creation (optional)

## Dependencies

- Jmix Core (`io.jmix.core`)
- Jmix FlowUI (`io.jmix.flowui`)
- Vaadin Spreadsheet (`com.vaadin:vaadin-spreadsheet-flow`)

## Usage in Applications

Typical usage is through `SpreadsheetComponent.configure(SpreadsheetComponentConfig)` in a view controller.
The `SpreadsheetControllerFactory` is still available for manual controller creation when needed.

## Extracting as a Separate Library

To extract this code as a standalone JAR:

1. Copy the `com.hexstyle.jmixspreadsheet` packages
2. Copy `src/main/resources/META-INF/resources/schemas/spreadsheet.xsd`
3. Ensure Spring component scanning includes the spreadsheet packages
4. Add the required dependencies listed above
