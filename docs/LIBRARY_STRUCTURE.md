# Jmix Spreadsheet Library Structure

This document describes the structure of the Jmix Spreadsheet component library.

## Library Package Structure

The core spreadsheet library code is located in:
```
com.company.jmixspreadsheet.spreadsheet.*
```

This package contains all the reusable spreadsheet component code that can be extracted as a separate Jmix library.

## Package Organization

### Library Code (Extractable)

- **`com.company.jmixspreadsheet.spreadsheet.api`** - Public API interfaces and classes
- **`com.company.jmixspreadsheet.spreadsheet.datasource`** - Data source adapters
- **`com.company.jmixspreadsheet.spreadsheet.layout`** - Layout engine and builders
- **`com.company.jmixspreadsheet.spreadsheet.index`** - Layout indexing
- **`com.company.jmixspreadsheet.spreadsheet.render`** - Rendering components
- **`com.company.jmixspreadsheet.spreadsheet.edit`** - Cell editing logic
- **`com.company.jmixspreadsheet.spreadsheet.diff`** - Change detection and delta computation
- **`com.company.jmixspreadsheet.spreadsheet.ui`** - UI components and factories
- **`com.company.jmixspreadsheet.spreadsheet.internal`** - Internal implementation (not part of public API)

### Application/Demo Code (Not Part of Library)

- **`com.company.jmixspreadsheet.entity.*`** - Application entities (Shipment, Plant, Product, etc.)
- **`com.company.jmixspreadsheet.view.*`** - Application views
- **`com.company.jmixspreadsheet.service.*`** - Application services
- **`com.company.jmixspreadsheet.security.*`** - Application security configuration

## Spring Configuration

The library uses Spring annotations for component registration:

- **`SpreadsheetComponentRegistration`** - Registers the SpreadsheetComponent with Jmix FlowUI
- **`SpreadsheetControllerFactory`** - Factory bean for creating controllers (annotated with `@Component`)

These configurations are automatically picked up by Spring component scanning when the library is included as a dependency.

## Dependencies

The library depends on:
- Jmix Core (`io.jmix.core`)
- Jmix FlowUI (`io.jmix.flowui`)
- Vaadin Spreadsheet (`com.vaadin:vaadin-spreadsheet-flow`)

## Usage in Applications

To use the spreadsheet library in a Jmix application:

1. Add the library as a dependency
2. Use `SpreadsheetControllerFactory` to create controllers
3. Use `SpreadsheetComponent` in XML view descriptors
4. Implement `PivotEditStrategy` for custom pivot editing logic

See the application code in `com.company.jmixspreadsheet.view.shipment.ShipmentSpreadsheetView` for usage examples.

## Extracting as a Separate Library

To extract this code as a separate Jmix library:

1. Create a new Gradle module/project
2. Copy all files from `com.company.jmixspreadsheet.spreadsheet.*` packages
3. Update `build.gradle` to build as a library (JAR) instead of application
4. Remove application-specific dependencies (if any)
5. Ensure Spring auto-configuration is properly set up
6. Publish to a Maven repository
