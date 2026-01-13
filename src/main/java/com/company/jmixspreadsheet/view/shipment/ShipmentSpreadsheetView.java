package com.company.jmixspreadsheet.view.shipment;

import com.company.jmixspreadsheet.entity.Shipment;
import com.company.jmixspreadsheet.service.ShipmentPivotEditStrategy;
import com.company.jmixspreadsheet.service.ShipmentService;
import com.company.jmixspreadsheet.spreadsheet.api.*;
import com.company.jmixspreadsheet.spreadsheet.ui.SpreadsheetControllerFactory;
import com.company.jmixspreadsheet.spreadsheet.ui.component.SpreadsheetComponent;
import com.company.jmixspreadsheet.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Route(value = "shipments-spreadsheet", layout = MainView.class)
@ViewController("ShipmentSpreadsheet.list")
@ViewDescriptor("shipment-spreadsheet-view.xml")
@DialogMode(width = "64em")
public class ShipmentSpreadsheetView extends StandardView {

    @ViewComponent
    private SpreadsheetComponent<Shipment> shipmentsSpreadsheet;
    @ViewComponent
    private CollectionContainer<Shipment> shipmentsDc;
    @ViewComponent
    private CollectionLoader<Shipment> shipmentsDl;

    @Autowired
    private Messages messages;
    @Autowired
    private SpreadsheetControllerFactory controllerFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ShipmentService shipmentService;

    @ViewComponent
    private GenericFilter genericFilter;

    @Subscribe
    public void onInit(final InitEvent event) {
        setupSpreadsheet();
    }

    private void setupSpreadsheet() {
        // Create pivot table model with Plant, Product, Vessel as column axes
        SpreadsheetTableModel<Shipment> model = createPivotTableModel();

        // Create controller
        // Note: Filter change handling is implemented in the dataSourceAdapter
        // (see SpreadsheetControllerFactory.createDataSourceAdapter)
        // The controller uses DataManager for persistence and Metadata for entity creation
        SpreadsheetController<Shipment, CollectionContainer<Shipment>> controller =
                controllerFactory.createController(Shipment.class);

        // Bind controller to model and data
        // No DataContext needed - controller uses DataManager directly
        controller.bind(model, shipmentsDc);

        // Set controller on component
        shipmentsSpreadsheet.setController(controller);
    }

    private SpreadsheetTableModel<Shipment> createPivotTableModel() {
        // Create row axes: Day (generates rows from unique dates)
        List<PivotAxis<Shipment>> rowAxes = new ArrayList<>();
        
        // Create a comparator for Object values (assuming they are Comparable)
        Comparator<Object> objectComparator = (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                @SuppressWarnings("unchecked")
                Comparable<Object> c1 = (Comparable<Object>) o1;
                return c1.compareTo(o2);
            }
            return o1.toString().compareTo(o2.toString());
        };
        
        // Day axis - generates rows from unique dates
        // This creates one row per unique date in the filtered data
        PivotAxis<Shipment> dayAxis = new DefaultPivotAxis<>(
                "day",
                shipment -> shipment.getDay(), // Extract date as key
                objectComparator,
                PivotAxis.RenderMode.MERGED
        );
        rowAxes.add(dayAxis);
        
        // Create column axes: Plant, Product, Vessel
        List<PivotAxis<Shipment>> columnAxes = new ArrayList<>();
        
        // Plant axis
        PivotAxis<Shipment> plantAxis = new DefaultPivotAxis<>(
                "plant",
                shipment -> shipment.getPlant() != null ? shipment.getPlant().getName() : "",
                objectComparator,
                PivotAxis.RenderMode.MERGED
        );
        columnAxes.add(plantAxis);

        // Product axis
        PivotAxis<Shipment> productAxis = new DefaultPivotAxis<>(
                "product",
                shipment -> shipment.getProduct() != null ? shipment.getProduct().getName() : "",
                objectComparator,
                PivotAxis.RenderMode.MERGED
        );
        columnAxes.add(productAxis);

        // Vessel axis
        PivotAxis<Shipment> vesselAxis = new DefaultPivotAxis<>(
                "vessel",
                shipment -> shipment.getVessel() != null ? shipment.getVessel().getName() : "",
                objectComparator,
                PivotAxis.RenderMode.MERGED
        );
        columnAxes.add(vesselAxis);

        // Create measures: Value (SUM) and CumulativeSum (SUM)
        PivotMeasure<Shipment> valueMeasure = new DefaultPivotMeasure<>(
                "value",
                "Value",
                shipment -> shipment.getValue() != null ? shipment.getValue() : BigDecimal.ZERO,
                PivotMeasure.AggregationType.SUM,
                null
        );
        
        // CumulativeSum: calculate running total for each shipment using entities from collection
        // Use a supplier to access collection container dynamically (not captured at model creation time)
        // This ensures cumulative sum uses current entities when layout is rebuilt after edits
        java.util.function.Supplier<java.util.Collection<Shipment>> allShipmentsSupplier = () -> shipmentsDc.getItems();
        PivotMeasure<Shipment> cumulativeSumMeasure = new DefaultPivotMeasure<>(
                "cumulativeSum",
                "Cumulative Sum",
                shipment -> {
                    if (shipment.getDay() == null || shipment.getPlant() == null || 
                        shipment.getProduct() == null || shipment.getVessel() == null) {
                        return BigDecimal.ZERO;
                    }
                    // Calculate cumulative sum from entities in the collection (not from database)
                    // This ensures changes are reflected immediately when layout is rebuilt
                    return shipmentService.calculateRunningTotalFromCollection(
                            allShipmentsSupplier.get(),
                            shipment.getDay(),
                            shipment.getPlant(),
                            shipment.getProduct(),
                            shipment.getVessel()
                    );
                },
                PivotMeasure.AggregationType.CUSTOM,
                numbers -> {
                    // For cumulative sums, return the maximum value (final cumulative sum)
                    double max = Double.NEGATIVE_INFINITY;
                    for (Number num : numbers) {
                        if (num != null) {
                            double val = num.doubleValue();
                            if (val > max) {
                                max = val;
                            }
                        }
                    }
                    return max == Double.NEGATIVE_INFINITY ? BigDecimal.ZERO : BigDecimal.valueOf(max);
                }
        );
        
        // Extract date range from filter and create row completion supplier
        // This will generate rows for all dates in the filter range, even if no entities exist
        Optional<Supplier<List<Object>>> rowCompletion = createDateRangeCompletion();
        
        // Create edit strategy for pivot cells (injected as Spring component)
        ShipmentPivotEditStrategy editStrategy = applicationContext.getBean(ShipmentPivotEditStrategy.class);
        
        // Create pivot configuration with day as row axis
        SpreadsheetPivot<Shipment> pivot = new DefaultSpreadsheetPivot<>(
                rowAxes, // Day as row axis
                columnAxes,
                List.of(valueMeasure, cumulativeSumMeasure), // Two measures: Value and CumulativeSum
                editStrategy,
                rowCompletion,
                Optional.empty() // No column completion
        );

        // Create regular columns (not in pivot)
        // Day is now in row axis, so we don't need it as a regular column
        List<SpreadsheetColumn<Shipment>> columns = new ArrayList<>();

        // Create table model
        return new DefaultSpreadsheetTableModel<>(
                Shipment.class,
                columns,
                null, // No grouping
                null, // No filter
                null, // No sort
                Optional.of(pivot),
                null // No interaction handler
        );
    }

    /**
     * Creates a supplier that provides all dates in the filter range for row completion.
     * <p>
     * This supplier is used by the layout builder to generate rows for all dates in the range,
     * even if no entities exist for those dates. The supplier re-extracts the date range
     * from the filter each time it's called (when layout is built), ensuring it always uses
     * the current filter values.
     * <p>
     * Returns a supplier that will complete rows with dates only when both filter fields are set.
     * The supplier extracts the date range dynamically on each call, so it adapts to filter changes.
     *
     * @return optional supplier of date values
     */
    private Optional<Supplier<List<Object>>> createDateRangeCompletion() {
        // Return a supplier that extracts date range on each call (when layout is built)
        // This ensures it uses the current filter values, even if they change after model creation
        // The supplier will only generate dates when both "from" and "to" fields are set
        return Optional.of(() -> {
            LocalDate[] dateRange = new LocalDate[2]; // [0] = from, [1] = to

            // Extract date range from filter
            if (genericFilter != null) {
                var configuration = genericFilter.getCurrentConfiguration();
                if (configuration != null) {
                    var rootComponent = configuration.getRootLogicalFilterComponent();
                    if (rootComponent != null) {
                        extractDateRangeFromComponent(rootComponent, dateRange);
                    }
                }
            }

            // Also try to get from loader parameters as fallback
            if (dateRange[0] == null || dateRange[1] == null) {
                var loaderParams = shipmentsDl.getParameters();
                if (loaderParams != null) {
                    Object dayFromParam = loaderParams.get("dayFrom");
                    Object dayToParam = loaderParams.get("dayTo");
                    
                    if (dayFromParam instanceof LocalDate) {
                        dateRange[0] = (LocalDate) dayFromParam;
                    }
                    if (dayToParam instanceof LocalDate) {
                        dateRange[1] = (LocalDate) dayToParam;
                    }
                }
            }

            // Generate all dates in the range only if BOTH dates are available
            if (dateRange[0] != null && dateRange[1] != null) {
                List<Object> dates = new ArrayList<>();
                LocalDate current = dateRange[0];
                LocalDate toDate = dateRange[1];
                while (!current.isAfter(toDate)) {
                    dates.add(current);
                    current = current.plusDays(1);
                }
                return dates;
            }

            // Return empty list if date range not fully set (one or both fields missing)
            return new ArrayList<>();
        });
    }

    /**
     * Recursively extracts date range from filter components.
     */
    private void extractDateRangeFromComponent(
            io.jmix.flowui.component.logicalfilter.LogicalFilterComponent<?> component,
            LocalDate[] dateRange) {
        
        if (component == null) {
            return;
        }

        // Check if this is a PropertyFilter for "day" property
        if (component instanceof PropertyFilter) {
            PropertyFilter<?> propertyFilter = (PropertyFilter<?>) component;
            if ("day".equals(propertyFilter.getProperty())) {
                Object value = propertyFilter.getValue();
                if (value instanceof LocalDate) {
                    LocalDate dateValue = (LocalDate) value;
                    var operation = propertyFilter.getOperation();
                    
                    if (operation == PropertyFilter.Operation.GREATER_OR_EQUAL) {
                        dateRange[0] = dateValue;
                    } else if (operation == PropertyFilter.Operation.LESS_OR_EQUAL) {
                        dateRange[1] = dateValue;
                    }
                }
            }
        }

        // Recurse to child components
        var children = component.getOwnFilterComponents();
        if (children != null) {
            for (var child : children) {
                // Check if child is a LogicalFilterComponent
                if (child instanceof io.jmix.flowui.component.logicalfilter.LogicalFilterComponent) {
                    extractDateRangeFromComponent(
                            (io.jmix.flowui.component.logicalfilter.LogicalFilterComponent<?>) child, 
                            dateRange);
                } else if (child instanceof PropertyFilter) {
                    // Handle PropertyFilter directly
                    PropertyFilter<?> propertyFilter = (PropertyFilter<?>) child;
                    if ("day".equals(propertyFilter.getProperty())) {
                        Object value = propertyFilter.getValue();
                        if (value instanceof LocalDate) {
                            LocalDate dateValue = (LocalDate) value;
                            var operation = propertyFilter.getOperation();
                            
                            if (operation == PropertyFilter.Operation.GREATER_OR_EQUAL) {
                                dateRange[0] = dateValue;
                            } else if (operation == PropertyFilter.Operation.LESS_OR_EQUAL) {
                                dateRange[1] = dateValue;
                            }
                        }
                    }
                }
            }
        }
    }
}
