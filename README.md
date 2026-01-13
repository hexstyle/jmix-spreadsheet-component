# Jmix Spreadsheet Component

A powerful Jmix FlowUI component that provides spreadsheet functionality with pivot table support, declarative configuration, and incremental updates.

## Features

- 📊 **Pivot Tables** - Create pivot tables with multiple row and column axes
- ✏️ **Cell Editing** - Edit cells directly in the spreadsheet with automatic persistence
- 🔄 **Incremental Updates** - Efficient updates without full re-rendering
- 📋 **DataGrid-like API** - Declarative configuration similar to Jmix DataGrid
- 🎯 **Type-Safe** - Full type safety with generic entity types
- 🔌 **Jmix Integration** - Seamless integration with Jmix DataComponents (CollectionContainer, DataManager)
- 📈 **Aggregations** - Support for SUM, COUNT, AVG, and custom aggregations
- 🎨 **Flexible Layout** - Customizable headers, merged regions, and styling

## Prerequisites

- Java 17 or higher
- Jmix Framework 2.7.2 or higher
- Vaadin Spreadsheet add-on (com.vaadin:vaadin-spreadsheet-flow:24.9.8)
- Gradle 7.0 or higher (for building)

## Getting Started

### Building and Running the Demo Application

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd jmix-spreadsheet-controller
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application:**
   - Open your browser and navigate to `http://localhost:8080`
   - Login with default credentials (configured in your security setup)
   - Navigate to the "Shipments Spreadsheet" view to see the component in action

### Demo Application

The demo application includes:
- **Shipments Spreadsheet View** - A complete example with pivot tables showing shipments data
- Sample entities (Shipment, Plant, Product, Vessel)
- Custom pivot edit strategy implementation
- Filter integration with date range completion

## Integration into Your Project

### Step 1: Add Dependencies

Add the Vaadin Spreadsheet dependency to your `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'com.vaadin:vaadin-spreadsheet-flow:24.9.8'
}
```

### Step 2: Copy Library Code

Copy the spreadsheet library code from `src/main/java/com/company/jmixspreadsheet/spreadsheet/` to your project, or extract it as a separate library module.

**Required packages:**
- `com.company.jmixspreadsheet.spreadsheet.api`
- `com.company.jmixspreadsheet.spreadsheet.datasource`
- `com.company.jmixspreadsheet.spreadsheet.layout`
- `com.company.jmixspreadsheet.spreadsheet.index`
- `com.company.jmixspreadsheet.spreadsheet.render`
- `com.company.jmixspreadsheet.spreadsheet.edit`
- `com.company.jmixspreadsheet.spreadsheet.diff`
- `com.company.jmixspreadsheet.spreadsheet.ui`
- `com.company.jmixspreadsheet.spreadsheet.internal`

### Step 3: Configure Spring

The component is automatically registered via `SpreadsheetComponentRegistration`. Ensure component scanning includes the spreadsheet packages, or the configuration will be picked up automatically if placed in the default package structure.

### Step 4: Create a View with Spreadsheet

#### 4.1 Create the XML View Descriptor

Create a view XML file (e.g., `my-spreadsheet-view.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      xmlns:spreadsheet="http://com.company/spreadsheet.xsd">
    <layout>
        <spreadsheet:spreadsheet id="mySpreadsheet" 
                                 width="100%" 
                                 height="100%"/>
    </layout>
</view>
```

**Note:** The custom namespace `xmlns:spreadsheet="http://com.company/spreadsheet.xsd"` is used for the spreadsheet component. Ensure the schema is registered in your project (see `SpreadsheetComponentRegistration`).

#### 4.2 Create the View Controller

Create a view controller class:

```java
@Route(value = "my-spreadsheet", layout = MainView.class)
@ViewController("MyEntity.spreadsheet")
@ViewDescriptor("my-spreadsheet-view.xml")
public class MySpreadsheetView extends StandardView {

    @ViewComponent
    private SpreadsheetComponent<MyEntity> mySpreadsheet;
    
    @ViewComponent
    private CollectionContainer<MyEntity> myEntitiesDc;
    
    @Autowired
    private SpreadsheetControllerFactory controllerFactory;
    
    @Subscribe
    public void onInit(InitEvent event) {
        setupSpreadsheet();
    }
    
    private void setupSpreadsheet() {
        // Create table model
        SpreadsheetTableModel<MyEntity> model = createTableModel();
        
        // Create controller
        SpreadsheetController<MyEntity, CollectionContainer<MyEntity>> controller =
                controllerFactory.createController(MyEntity.class);
        
        // Bind controller to model and data
        controller.bind(model, myEntitiesDc);
        
        // Set controller on component
        mySpreadsheet.setController(controller);
    }
    
    private SpreadsheetTableModel<MyEntity> createTableModel() {
        // Define columns, pivot configuration, etc.
        // See examples below
        return new DefaultSpreadsheetTableModel<>(...);
    }
}
```

## Basic Usage Examples

### Simple Flat Table

```java
private SpreadsheetTableModel<MyEntity> createFlatTableModel() {
    // Define columns
    List<SpreadsheetColumn<MyEntity>> columns = new ArrayList<>();
    
    columns.add(new DefaultSpreadsheetColumn<>(
        "id",
        "ID",
        MyEntity::getId,
        null, // no setter (read-only)
        null, // no formatter
        null, // default width
        null, // default alignment
        false // not editable
    ));
    
    columns.add(new DefaultSpreadsheetColumn<>(
        "name",
        "Name",
        MyEntity::getName,
        MyEntity::setName, // editable
        null,
        null,
        null,
        true
    ));
    
    return new DefaultSpreadsheetTableModel<>(
        MyEntity.class,
        columns,
        null, // no grouping
        null, // no filter
        null, // no sort
        Optional.empty(), // no pivot
        null // no interaction handler
    );
}
```

### Pivot Table Example

```java
private SpreadsheetTableModel<MyEntity> createPivotTableModel() {
    // Create row axes
    List<PivotAxis<MyEntity>> rowAxes = new ArrayList<>();
    PivotAxis<MyEntity> categoryAxis = new DefaultPivotAxis<>(
        "category",
        entity -> entity.getCategory(),
        Comparator.naturalOrder(),
        PivotAxis.RenderMode.MERGED
    );
    rowAxes.add(categoryAxis);
    
    // Create column axes
    List<PivotAxis<MyEntity>> columnAxes = new ArrayList<>();
    PivotAxis<MyEntity> regionAxis = new DefaultPivotAxis<>(
        "region",
        entity -> entity.getRegion(),
        Comparator.naturalOrder(),
        PivotAxis.RenderMode.MERGED
    );
    columnAxes.add(regionAxis);
    
    // Create measures
    List<PivotMeasure<MyEntity>> measures = new ArrayList<>();
    PivotMeasure<MyEntity> amountMeasure = new DefaultPivotMeasure<>(
        "amount",
        "Amount",
        entity -> entity.getAmount() != null ? entity.getAmount() : BigDecimal.ZERO,
        PivotMeasure.AggregationType.SUM,
        null
    );
    measures.add(amountMeasure);
    
    // Create pivot configuration
    SpreadsheetPivot<MyEntity> pivot = new DefaultSpreadsheetPivot<>(
        rowAxes,
        columnAxes,
        measures,
        null // no edit strategy (read-only)
    );
    
    // Create table model
    return new DefaultSpreadsheetTableModel<>(
        MyEntity.class,
        new ArrayList<>(), // no regular columns for pivot-only view
        null,
        null,
        null,
        Optional.of(pivot),
        null
    );
}
```

### Editable Pivot Table

To enable editing in pivot tables, implement `PivotEditStrategy`:

```java
@Component("myapp_MyPivotEditStrategy")
public class MyPivotEditStrategy implements PivotEditStrategy<MyEntity> {
    
    @Override
    public Map<MyEntity, Map<String, Object>> determineEdits(
            PivotEditContext<MyEntity> context, 
            Object newValue) {
        Map<MyEntity, Map<String, Object>> edits = new HashMap<>();
        
        List<MyEntity> contributingEntities = context.getContributingEntities();
        if (contributingEntities.isEmpty()) {
            // Create new entity for empty pivot cell
            MyEntity newEntity = createNewEntity(context, newValue);
            Map<String, Object> propertyUpdates = new HashMap<>();
            propertyUpdates.put("amount", newValue);
            edits.put(newEntity, propertyUpdates);
        } else {
            // Update existing entity
            MyEntity entity = contributingEntities.get(0);
            Map<String, Object> propertyUpdates = new HashMap<>();
            propertyUpdates.put("amount", newValue);
            edits.put(entity, propertyUpdates);
        }
        
        return edits;
    }
    
    private MyEntity createNewEntity(PivotEditContext<MyEntity> context, Object newValue) {
        // Create and configure new entity based on pivot context
        // ...
    }
}
```

Then use it in the pivot configuration:

```java
@Autowired
private MyPivotEditStrategy editStrategy;

SpreadsheetPivot<MyEntity> pivot = new DefaultSpreadsheetPivot<>(
    rowAxes,
    columnAxes,
    measures,
    editStrategy // enable editing
);
```

### Row/Column Completion

Generate rows or columns from external data sources (e.g., date ranges):

```java
// Generate rows for all dates in a range
Optional<Supplier<List<Object>>> rowCompletion = Optional.of(() -> {
    LocalDate from = getFromDate();
    LocalDate to = getToDate();
    
    List<Object> dates = new ArrayList<>();
    LocalDate current = from;
    while (!current.isAfter(to)) {
        dates.add(current);
        current = current.plusDays(1);
    }
    return dates;
});

SpreadsheetPivot<MyEntity> pivot = new DefaultSpreadsheetPivot<>(
    rowAxes,
    columnAxes,
    measures,
    editStrategy,
    rowCompletion, // complete rows
    Optional.empty() // no column completion
);
```

## Advanced Features

### Incremental Updates

The component automatically performs incremental updates when entities change. The controller tracks affected entities and updates only the changed cells without full re-rendering.

### Custom Aggregations

Implement custom aggregation logic using `PivotMeasure.AggregationType.CUSTOM`:

```java
PivotMeasure<MyEntity> customMeasure = new DefaultPivotMeasure<>(
    "custom",
    "Custom",
    entity -> entity.getValue(),
    PivotMeasure.AggregationType.CUSTOM,
    (entities, valueProvider) -> {
        // Custom aggregation logic
        return entities.stream()
            .map(valueProvider)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
);
```

### Filter Integration

The component integrates with Jmix GenericFilter. Filter changes are automatically detected and the spreadsheet is reloaded:

```java
@ViewComponent
private GenericFilter genericFilter;

@ViewComponent
private CollectionLoader<MyEntity> myEntitiesDl;

// Filter is automatically connected to CollectionLoader
// Spreadsheet updates when filter changes
```

## Architecture

For detailed architecture documentation, see [docs/spreadsheet-architecture.md](docs/spreadsheet-architecture.md).

### Key Components

- **SpreadsheetController** - Main controller coordinating all components
- **SpreadsheetTableModel** - Declarative model for spreadsheet configuration
- **SpreadsheetPivot** - Pivot table configuration
- **PivotEditStrategy** - Strategy for handling pivot cell edits
- **LayoutEngine** - Builds spreadsheet layout from entities
- **SpreadsheetRenderer** - Renders layout to Vaadin Spreadsheet component

## API Documentation

The main public API classes are in the `com.company.jmixspreadsheet.spreadsheet.api` package:

- `SpreadsheetController<E, DC>` - Controller interface
- `SpreadsheetTableModel<E>` - Table model interface
- `SpreadsheetPivot<E>` - Pivot configuration interface
- `SpreadsheetColumn<E>` - Column definition
- `PivotAxis<E>` - Pivot axis definition
- `PivotMeasure<E>` - Measure definition
- `PivotEditStrategy<E>` - Edit strategy interface

## Examples

See the demo application for complete examples:

- **ShipmentSpreadsheetView** - Complete pivot table example with editing
- **ShipmentPivotEditStrategy** - Custom edit strategy implementation
- Date range completion with filter integration

## Troubleshooting

### Component Not Rendering

- Ensure `SpreadsheetComponentRegistration` is scanned by Spring
- Check that the controller is properly bound: `controller.bind(model, container)`
- Verify the component has the controller set: `component.setController(controller)`

### Cells Not Editable

- For pivot tables, ensure an `PivotEditStrategy` is provided
- For flat tables, ensure columns have setters and are marked as editable

### Incremental Updates Not Working

- Check that entities are saved via `DataManager.saveAll()`
- Verify `updateAffectedCells()` is called after entity changes
- Ensure entity keys are properly provided

## License

MIT