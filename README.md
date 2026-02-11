# Jmix Spreadsheet Component

Jmix FlowUI wrapper around Vaadin Spreadsheet with declarative configuration, pivot support, custom layouts, and incremental updates.

## Features

- Pivot tables with row and column axes
- Flat tables with editable cells
- Declarative SpreadsheetTableModel
- Incremental updates in table mode
- Custom layout mode for complex spreadsheets
- Jmix DataComponents integration
- Conditional styling and header customization

## Prerequisites

- Java 17 or higher
- Jmix Framework 2.7.2 or higher
- Vaadin Spreadsheet add-on (com.vaadin:vaadin-spreadsheet-flow:24.9.8)
- Gradle 7.0 or higher (for building)

## Quick Start (Table Mode)

1. Add the dependency to `build.gradle`:

```groovy
dependencies {
    implementation "com.vaadin:vaadin-spreadsheet-flow:24.9.8"
}
```

2. Declare the component in the view XML:

```xml
<view xmlns="http://jmix.io/schema/flowui/view"
      xmlns:spreadsheet="http://com.company/spreadsheet.xsd">
    <layout>
        <spreadsheet:spreadsheet id="ordersSpreadsheet"
                                 width="100%"
                                 height="100%"/>
    </layout>
</view>
```

3. Configure the component in the view controller:

```java
@ViewComponent
private SpreadsheetComponent<Order> ordersSpreadsheet;

@ViewComponent
private CollectionContainer<Order> ordersDc;

@Subscribe
public void onInit(InitEvent event) {
    SpreadsheetTableModel<Order> model = new DefaultSpreadsheetTableModel<>(
            Order.class,
            List.of(
                    new DefaultSpreadsheetColumn<>("id", "ID", Order::getId,
                            null, null, 120, null, false),
                    new DefaultSpreadsheetColumn<>("amount", "Amount", Order::getAmount,
                            Order::setAmount, null, 120, null, true)
            ),
            null,
            null,
            null,
            Optional.empty(),
            null
    );

    SpreadsheetComponentConfig<Order> config = SpreadsheetComponentConfig
            .forTable(Order.class, model, ordersDc)
            .withReadOnly(false)
            .withNavigationGridVisible(true);

    ordersSpreadsheet.configure(config);
}
```

Call `ordersSpreadsheet.reload()` whenever the data or filters change and the layout must be rebuilt.

## Custom Layout Mode

Use layout mode when you already have a layout builder and want full control over cells and merges.

```java
SpreadsheetComponentConfig<PortBalanceCell> config = SpreadsheetComponentConfig
        .forLayout(PortBalanceCell.class, this::buildPortBalanceLayout)
        .withReadOnly(true)
        .withNavigationGridVisible(false)
        .withCellKeyProvider(this::portBalanceCellKey)
        .addHeaderWidthDelta("Laycan", 100);

shipmentsSpreadsheet.configure(config);
```

`buildPortBalanceLayout()` must return a `SpreadsheetLayout<PortBalanceCell>`.

## Pivot Tables

Create a pivot model and pass it into the table model:

```java
SpreadsheetPivot<Order> pivot = new DefaultSpreadsheetPivot<>(
        rowAxes,
        columnAxes,
        measures,
        editStrategy
);

SpreadsheetTableModel<Order> model = new DefaultSpreadsheetTableModel<>(
        Order.class,
        List.of(),
        null,
        null,
        null,
        Optional.of(pivot),
        null
);
```

## Configuration Options

`SpreadsheetComponentConfig` and XML attributes cover the same defaults. Config values override XML values.

Common options:
- `readOnly` / `read-only`
- `navigationGridVisible` / `navigation-grid-visible`
- `autoRefreshViewport` / `auto-refresh-viewport`
- `autoResize` / `auto-resize`
- `headerStyle` / `header-style`
- `withHeaderRowIndex`, `addHeaderWidthOverride`, `addHeaderWidthDelta`
- `withCellKeyProvider` (layout mode)
- `addStyleRule` and `withStyleProvider` (conditional styling)
- `afterRender` (post render hook)

Example styling:

```java
SpreadsheetComponentConfig<Order> config = SpreadsheetComponentConfig
        .forTable(Order.class, model, ordersDc)
        .addStyleRule(StyleRule.of(StyleToken.NEGATIVE, ctx -> {
            Object value = ctx.getValue();
            return value instanceof Number n && n.doubleValue() < 0;
        }))
        .withStyleProvider(token -> {
            if (token == StyleToken.NEGATIVE) {
                return "color:#b91c1c;font-weight:600;";
            }
            return null;
        });
```

## Troubleshooting

- Component not rendering: ensure `configure` is called and the data container is loaded.
- Cells not editable: `readOnly` must be false and columns must have setters (pivot requires edit strategy).
- Layout mode updates: call `SpreadsheetComponent.reload()` after changing layout inputs.

## Architecture

See `docs/spreadsheet-architecture.md` and `docs/LIBRARY_STRUCTURE.md`.

## License and Vaadin Spreadsheet

Vaadin Spreadsheet is commercial and licensed separately from this repository. See `THIRD_PARTY_NOTICES.md`.
