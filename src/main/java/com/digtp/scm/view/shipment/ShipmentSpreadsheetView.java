package com.digtp.scm.view.shipment;

import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.ShippingCombination;
import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.WarehouseTerminal;
import com.digtp.scm.entity.Movement;
import com.digtp.scm.portbalance.aggregate.PortBalanceAggregator;
import com.digtp.scm.portbalance.aggregate.PortBalanceCell;
import com.digtp.scm.portbalance.aggregate.PortBalanceTable;
import com.digtp.scm.portbalance.columns.PortBalanceColumnKey;
import com.digtp.scm.portbalance.columns.PortBalanceMetric;
import com.digtp.scm.portbalance.columns.TrackKey;
import com.digtp.scm.portbalance.layout.PortBalanceLayoutBuilder;
import com.digtp.scm.portbalance.layout.PortBalanceCellBinding;
import com.digtp.scm.portbalance.layout.PortBalanceCellContext;
import com.digtp.scm.portbalance.query.PortBalanceQueryService;
import com.digtp.scm.portbalance.query.VesselLoadingOutRecord;
import com.digtp.scm.portbalance.ui.PortBalanceSpreadsheetController;
import com.digtp.scm.view.portbalance.PortBalanceCellDetailsView;
import com.digtp.scm.view.main.MainView;
import com.hexstyle.jmixspreadsheet.api.DefaultSpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.internal.InteractionContextImpl;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Route(value = "shipments-spreadsheet", layout = MainView.class)
@ViewController("ShipmentSpreadsheet.list")
@ViewDescriptor(value = "/com/digtp/scm/view/shipment/shipment-spreadsheet-view.xml", path = "/com/digtp/scm/view/shipment/shipment-spreadsheet-view.xml")
@DialogMode(width = "64em")
public class ShipmentSpreadsheetView extends StandardView {

    @ViewComponent
    private SpreadsheetComponent<PortBalanceCell> shipmentsSpreadsheet;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PortBalanceQueryService queryService;

    @Autowired
    private PortBalanceAggregator aggregator;

    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private GenericFilter genericFilter;

    @ViewComponent
    private MultiSelectComboBox<PortBalanceMetric> metricsSelector;

    private final PortBalanceLayoutBuilder layoutBuilder = new PortBalanceLayoutBuilder();
    private PortBalanceSpreadsheetController controller;
    private boolean adjustingMetrics;
    private final Set<PropertyFilter<?>> boundFilters = new HashSet<>();
    private static final List<PortBalanceMetric> METRIC_ORDER = List.of(
            PortBalanceMetric.IN,
            PortBalanceMetric.OUT,
            PortBalanceMetric.STOCK
    );

    @Subscribe
    public void onInit(final InitEvent event) {
        setupMetricSelector();
        setupSpreadsheet();
        setupFilterListeners();
    }

    private void setupSpreadsheet() {
        SpreadsheetInteractionHandler<PortBalanceCell> interactionHandler = createInteractionHandler();
        SpreadsheetTableModel<PortBalanceCell> model = new DefaultSpreadsheetTableModel<>(
                PortBalanceCell.class,
                List.of(),
                null,
                null,
                null,
                Optional.empty(),
                interactionHandler
        );

        controller = new PortBalanceSpreadsheetController(this::buildPortBalanceTable, layoutBuilder);
        controller.bind(model, new Object());
        shipmentsSpreadsheet.setController(controller);
    }

    private SpreadsheetInteractionHandler<PortBalanceCell> createInteractionHandler() {
        return new SpreadsheetInteractionHandler<>() {
            @Override
            public void onCellClick(InteractionContext<PortBalanceCell> context) {
                if (!(context instanceof InteractionContextImpl<PortBalanceCell> impl)) {
                    return;
                }
                if (!(impl.getCellBinding() instanceof PortBalanceCellBinding binding)) {
                    return;
                }
                if (!(binding.getPivotContext() instanceof PortBalanceCellContext cellContext)) {
                    return;
                }
                if (cellContext.isMonthBreak()) {
                    return;
                }
                PortBalanceMetric metric = cellContext.getMetric();
                if (!isClickableMetric(metric) || cellContext.getDate() == null) {
                    return;
                }
                PortBalanceCellContext effectiveContext = applySelectedTrack(cellContext);
                dialogWindows.view(ShipmentSpreadsheetView.this, PortBalanceCellDetailsView.class)
                        .withViewConfigurer(view -> view.setContext(effectiveContext))
                        .open();
            }
        };
    }

    private boolean isClickableMetric(PortBalanceMetric metric) {
        return metric == PortBalanceMetric.IN
                || metric == PortBalanceMetric.OUT
                || metric == PortBalanceMetric.STOCK
                || metric == PortBalanceMetric.VESSEL
                || metric == PortBalanceMetric.LAYCAN
                || metric == PortBalanceMetric.TOTAL_OUT;
    }

    private PortBalanceCellContext applySelectedTrack(PortBalanceCellContext context) {
        if (context == null) {
            return context;
        }
        Track selectedTrack = resolveSelectedTrack();
        if (selectedTrack == null) {
            return context;
        }
        return new PortBalanceCellContext(
                context.getDate(),
                TrackKey.from(selectedTrack),
                context.getTerminal(),
                context.getTransportType(),
                context.getMetric(),
                context.getVessel(),
                context.getComboKey(),
                context.isMonthBreak(),
                context.isVesselDetail(),
                context.getVesselId(),
                context.getVesselName()
        );
    }

    private Track resolveSelectedTrack() {
        Track selectedTrack = resolveFilters().track;
        if (selectedTrack == null || selectedTrack.getId() == null) {
            return selectedTrack;
        }
        return dataManager.load(Track.class)
                .id(selectedTrack.getId())
                .fetchPlan(fetchPlan -> fetchPlan.add("name").add("trackType"))
                .optional()
                .orElse(selectedTrack);
    }

    private void setupMetricSelector() {
        if (metricsSelector == null) {
            return;
        }
        metricsSelector.setItems(METRIC_ORDER);
        metricsSelector.setItemLabelGenerator(PortBalanceMetric::name);
        metricsSelector.setValue(EnumSet.of(
                PortBalanceMetric.IN,
                PortBalanceMetric.OUT,
                PortBalanceMetric.STOCK
        ));
        metricsSelector.addValueChangeListener(event -> {
            if (adjustingMetrics) {
                return;
            }
            if (event.getValue() != null && event.getValue().size() > 3) {
                adjustingMetrics = true;
                metricsSelector.setValue(event.getOldValue());
                adjustingMetrics = false;
                return;
            }
            reloadPortBalance();
        });
    }

    private void setupFilterListeners() {
        if (genericFilter == null) {
            return;
        }
        genericFilter.addConfigurationRefreshListener(event -> {
            attachFilterValueListeners();
            if (genericFilter.isAutoApply()) {
                reloadPortBalance();
            }
        });
        genericFilter.addConfigurationChangeListener(event -> {
            attachFilterValueListeners();
            if (genericFilter.isAutoApply()) {
                reloadPortBalance();
            }
        });
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        if (genericFilter != null) {
            applyDefaultFilterConfiguration();
            attachFilterValueListeners();
        }
    }

    @Subscribe(id = "movementsDl", target = Target.DATA_LOADER)
    private void onMovementsDlPostLoad(CollectionLoader.PostLoadEvent<Movement> event) {
        reloadPortBalance();
    }

    private void applyDefaultFilterConfiguration() {
        var current = genericFilter.getCurrentConfiguration();
        if (current != null && current != genericFilter.getEmptyConfiguration()) {
            return;
        }
        var defaultConfiguration = genericFilter.getConfiguration("defaultConfiguration");
        if (defaultConfiguration != null) {
            genericFilter.setCurrentConfiguration(defaultConfiguration);
        }
    }

    private void attachFilterValueListeners() {
        boundFilters.clear();
        var configuration = genericFilter.getCurrentConfiguration();
        if (configuration == null) {
            return;
        }
        var root = configuration.getRootLogicalFilterComponent();
        if (root == null) {
            return;
        }
        bindFilterListeners(root);
    }

    private void bindFilterListeners(io.jmix.flowui.component.logicalfilter.LogicalFilterComponent<?> component) {
        if (component == null) {
            return;
        }
        if (component instanceof PropertyFilter<?> propertyFilter) {
            addFilterListener(propertyFilter);
        }
        var children = component.getOwnFilterComponents();
        if (children == null) {
            return;
        }
        for (var child : children) {
            if (child instanceof io.jmix.flowui.component.logicalfilter.LogicalFilterComponent<?> childLogical) {
                bindFilterListeners(childLogical);
            } else if (child instanceof PropertyFilter<?> propertyFilter) {
                addFilterListener(propertyFilter);
            }
        }
    }

    private void addFilterListener(PropertyFilter<?> propertyFilter) {
        if (!boundFilters.add(propertyFilter)) {
            return;
        }
        propertyFilter.addValueChangeListener(event -> {
            if (genericFilter != null && !genericFilter.isAutoApply()) {
                return;
            }
            reloadPortBalance();
        });
    }

    private void reloadPortBalance() {
        if (controller != null) {
            controller.reload();
        }
    }

    private PortBalanceTable buildPortBalanceTable() {
        PortBalanceFilter filter = resolveFilters();
        if (filter.from == null || filter.to == null) {
            return new PortBalanceTable(List.of(), List.of(), List.of());
        }

        List<Track> tracks = loadTracks(filter.track);
        if (tracks.isEmpty()) {
            return new PortBalanceTable(List.of(), List.of(), List.of());
        }

        List<ShippingCombination> combinations = loadCombinations(filter);
        List<ShippingCombination> filtered = filterCombinationsWithData(
                combinations, tracks, filter.from, filter.to);

        if (filtered.isEmpty()) {
            return new PortBalanceTable(List.of(), List.of(), List.of());
        }

        PortBalanceTable table = aggregator.aggregate(filtered, tracks, filter.from, filter.to);
        return filterMetrics(table, resolveSelectedMetrics());
    }

    private List<PortBalanceMetric> resolveSelectedMetrics() {
        if (metricsSelector == null || metricsSelector.getValue() == null || metricsSelector.getValue().isEmpty()) {
            return METRIC_ORDER;
        }
        Set<PortBalanceMetric> selected = metricsSelector.getValue();
        List<PortBalanceMetric> ordered = new ArrayList<>();
        for (PortBalanceMetric metric : METRIC_ORDER) {
            if (selected.contains(metric)) {
                ordered.add(metric);
            }
        }
        return ordered;
    }

    private PortBalanceTable filterMetrics(PortBalanceTable table, List<PortBalanceMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return table;
        }
        Set<PortBalanceMetric> fixed = EnumSet.of(
                PortBalanceMetric.VESSEL,
                PortBalanceMetric.LAYCAN,
                PortBalanceMetric.TOTAL_OUT
        );
        List<PortBalanceColumnKey> columns = table.getColumns().stream()
                .filter(key -> fixed.contains(key.metric()) || metrics.contains(key.metric()))
                .toList();
        Set<PortBalanceColumnKey> allowed = new HashSet<>(columns);
        List<PortBalanceCell> cells = table.getCells().stream()
                .filter(cell -> allowed.contains(cell.getColumnKey()))
                .toList();
        return new PortBalanceTable(table.getRows(), columns, cells);
    }

    private List<ShippingCombination> loadCombinations(PortBalanceFilter filter) {
        StringBuilder query = new StringBuilder("select c from scm_ShippingCombination c where 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.plant != null) {
            query.append(" and c.plant = :plant");
            params.put("plant", filter.plant);
        }
        if (filter.product != null) {
            query.append(" and c.product = :product");
            params.put("product", filter.product);
        }
        if (filter.productPackage != null) {
            query.append(" and c.productPackage = :productPackage");
            params.put("productPackage", filter.productPackage);
        }
        if (filter.terminal != null) {
            query.append(" and c.warehouse.terminal = :terminal");
            params.put("terminal", filter.terminal);
        }

        var loader = dataManager.load(ShippingCombination.class)
                .query(query.toString());

        params.forEach(loader::parameter);

        return loader.fetchPlan(fetchPlan -> fetchPlan
                .add("plant", plan -> plan.add("code"))
                .add("product", plan -> plan.add("name"))
                .add("productPackage", plan -> plan.add("code"))
                .add("transportType", plan -> plan.add("name"))
                .add("warehouse", plan -> plan.add("terminal", tPlan -> tPlan.add("code"))))
                .list();
    }

    private List<Track> loadTracks(Track selectedTrack) {
        if (selectedTrack != null) {
            return List.of(selectedTrack);
        }
        return dataManager.load(Track.class)
                .query("select t from scm_Track t")
                .fetchPlan(fetchPlan -> fetchPlan.add("name").add("trackType"))
                .list();
    }

    private List<ShippingCombination> filterCombinationsWithData(List<ShippingCombination> combinations,
                                                                 List<Track> tracks,
                                                                 LocalDate from,
                                                                 LocalDate to) {
        if (combinations.isEmpty() || tracks.isEmpty()) {
            return combinations;
        }

        List<ShippingCombination> filtered = new ArrayList<>();
        for (ShippingCombination combination : combinations) {
            if (hasInMovements(combination, tracks, from, to)
                    || hasOutRecords(combination, tracks, from, to)) {
                filtered.add(combination);
            }
        }
        return filtered;
    }

    private boolean hasInMovements(ShippingCombination combination,
                                   List<Track> tracks,
                                   LocalDate from,
                                   LocalDate to) {
        if (tracks.isEmpty()) {
            return false;
        }
        return !queryService.findPlantShipmentMovements(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouse(),
                combination.getTransportType(),
                tracks,
                from,
                to
        ).isEmpty();
    }

    private boolean hasOutRecords(ShippingCombination combination,
                                  List<Track> tracks,
                                  LocalDate from,
                                  LocalDate to) {
        WarehouseTerminal warehouse = combination.getWarehouse();
        Terminal terminal = warehouse == null ? null : warehouse.getTerminal();
        TransportType transportType = combination.getTransportType();
        if (terminal == null || transportType == null) {
            return false;
        }
        for (Track track : tracks) {
            List<VesselLoadingOutRecord> records = queryService.findVesselLoadingOutRecords(
                    track,
                    warehouse,
                    terminal,
                    transportType,
                    from,
                    to
            );
            if (!records.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private PortBalanceFilter resolveFilters() {
        FilterValues values = new FilterValues();
        if (genericFilter != null) {
            var configuration = genericFilter.getCurrentConfiguration();
            if (configuration != null) {
                var rootComponent = configuration.getRootLogicalFilterComponent();
                if (rootComponent != null) {
                    collectFilterValues(rootComponent, values);
                }
            }
        }
        return new PortBalanceFilter(values.from, values.to, values.track, values.terminal,
                values.product, values.plant, values.productPackage);
    }

    private void collectFilterValues(
            io.jmix.flowui.component.logicalfilter.LogicalFilterComponent<?> component,
            FilterValues values) {
        if (component == null) {
            return;
        }

        if (component instanceof PropertyFilter<?> propertyFilter) {
            applyPropertyFilter(propertyFilter, values);
        }

        var children = component.getOwnFilterComponents();
        if (children == null) {
            return;
        }
        for (var child : children) {
            if (child instanceof io.jmix.flowui.component.logicalfilter.LogicalFilterComponent<?> childLogical) {
                collectFilterValues(childLogical, values);
            } else if (child instanceof PropertyFilter<?> propertyFilter) {
                applyPropertyFilter(propertyFilter, values);
            }
        }
    }

    private void applyPropertyFilter(PropertyFilter<?> filter, FilterValues values) {
        String property = filter.getProperty();
        Object value = filter.getValue();
        if ("date".equals(property) && value instanceof LocalDate dateValue) {
            if (filter.getOperation() == PropertyFilter.Operation.GREATER_OR_EQUAL) {
                values.from = dateValue;
            } else if (filter.getOperation() == PropertyFilter.Operation.LESS_OR_EQUAL) {
                values.to = dateValue;
            }
        } else if ("track".equals(property) && value instanceof Track track) {
            values.track = track;
        } else if ("warehouse.terminal".equals(property) && value instanceof Terminal terminal) {
            values.terminal = terminal;
        } else if ("product".equals(property) && value instanceof Product product) {
            values.product = product;
        } else if ("originPlant".equals(property) && value instanceof Plant plant) {
            values.plant = plant;
        } else if ("productPackage".equals(property) && value instanceof ProductPackage productPackage) {
            values.productPackage = productPackage;
        }
    }

    private static final class FilterValues {
        private LocalDate from;
        private LocalDate to;
        private Track track;
        private Terminal terminal;
        private Product product;
        private Plant plant;
        private ProductPackage productPackage;
    }

    private record PortBalanceFilter(LocalDate from,
                                     LocalDate to,
                                     Track track,
                                     Terminal terminal,
                                     Product product,
                                     Plant plant,
                                     ProductPackage productPackage) {
    }
}
