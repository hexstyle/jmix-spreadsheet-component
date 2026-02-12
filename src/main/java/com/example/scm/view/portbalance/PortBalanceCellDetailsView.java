package com.example.scm.view.portbalance;

import com.example.scm.entity.Movement;
import com.example.scm.entity.PlantShipmentReason;
import com.example.scm.entity.ShippingCombination;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.Track;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import com.example.scm.portbalance.columns.ComboKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
import com.example.scm.portbalance.layout.PortBalanceCellContext;
import com.example.scm.portbalance.columns.TrackKey;
import com.example.scm.repository.MovementRepository;
import com.example.scm.view.main.MainView;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Route(value = "port-balance-cell-details", layout = MainView.class)
@ViewController("PortBalanceCellDetails.view")
@ViewDescriptor("/com/example/scm/view/portbalance/port-balance-cell-details-view.xml")
@DialogMode(width = "72em")
public class PortBalanceCellDetailsView extends StandardView {

    @ViewComponent
    private CollectionContainer<PlantShipmentReason> plantShipmentReasonsDc;
    @ViewComponent
    private CollectionContainer<VesselLoadItem> vesselLoadItemsDc;
    @ViewComponent
    private CollectionContainer<VesselLoad> vesselLoadsDc;
    @ViewComponent
    private Label plantShipmentsLabel;
    @ViewComponent
    private Label vesselLoadItemsLabel;
    @ViewComponent
    private Label vesselLoadsLabel;
    @ViewComponent
    private DataGrid<PlantShipmentReason> plantShipmentGrid;
    @ViewComponent
    private DataGrid<VesselLoadItem> vesselLoadItemGrid;
    @ViewComponent
    private DataGrid<VesselLoad> vesselLoadGrid;

    @Autowired
    private DataManager dataManager;
    @Autowired
    private MovementRepository movementRepository;
    @Autowired
    private FetchPlans fetchPlans;

    private PortBalanceCellContext context;
    private List<VesselLoadItem> loadedVesselLoadItems = List.of();

    public void setContext(PortBalanceCellContext context) {
        this.context = context;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        loadData();
    }

    private void loadData() {
        clearContainers();
        if (context == null || context.getDate() == null) {
            applyGridVisibility(null);
            return;
        }

        Track track = resolveTrack(context.getTrackKey());
        ComboKey comboKey = context.getComboKey();
        PortBalanceMetric metric = context.getMetric();
        if (requiresTrack(metric) && track == null) {
            return;
        }
        ShippingCombination combination = null;
        boolean requiresCombo = shouldLoadIn(metric) || shouldFilterOutByCombo(metric);
        if (requiresCombo) {
            if (track == null || comboKey == null) {
                return;
            }
            combination = resolveCombination(comboKey);
            if (combination == null) {
                return;
            }
        }
        if (shouldLoadIn(metric)) {
            loadPlantShipments(track, combination, context.getDate());
        }

        if (shouldLoadOut(metric)) {
            loadVesselLoads(track,
                    comboKey,
                    context.getTerminal(),
                    context.getTransportType(),
                    context.getDate(),
                    metric,
                    context.getVesselId(),
                    context.getVesselName(),
                    context.isVesselDetail());
        }
        applyGridVisibility(metric);
    }

    private void clearContainers() {
        plantShipmentReasonsDc.setItems(List.of());
        vesselLoadItemsDc.setItems(List.of());
        vesselLoadsDc.setItems(List.of());
        loadedVesselLoadItems = List.of();
    }

    private Track resolveTrack(TrackKey trackKey) {
        if (trackKey == null || trackKey.name() == null || trackKey.trackType() == null) {
            return null;
        }
        return dataManager.load(Track.class)
                .query("select t from scm_Track t where t.name = :name and t.trackType = :trackType")
                .parameter("name", trackKey.name())
                .parameter("trackType", trackKey.trackType())
                .optional()
                .orElse(null);
    }

    private ShippingCombination resolveCombination(ComboKey comboKey) {
        if (comboKey == null
                || !isPersisted(comboKey.plant())
                || !isPersisted(comboKey.product())
                || !isPersisted(comboKey.productPackage())
                || !isPersisted(comboKey.transportType())
                || !isPersisted(comboKey.terminal())) {
            return null;
        }
        return dataManager.load(ShippingCombination.class)
                .query("""
                        select c from scm_ShippingCombination c
                        where c.plant = :plant
                          and c.product = :product
                          and c.productPackage = :productPackage
                          and c.transportType = :transportType
                          and c.warehouse.terminal = :terminal
                        """)
                .parameter("plant", comboKey.plant())
                .parameter("product", comboKey.product())
                .parameter("productPackage", comboKey.productPackage())
                .parameter("transportType", comboKey.transportType())
                .parameter("terminal", comboKey.terminal())
                .fetchPlan(fetchPlan -> fetchPlan
                        .add("warehouse")
                        .add("transportType")
                        .add("plant")
                        .add("product")
                        .add("productPackage"))
                .optional()
                .orElse(null);
    }

    private void loadPlantShipments(Track track, ShippingCombination combination, LocalDate date) {
        FetchPlan fetchPlan = fetchPlans.builder(Movement.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("reason", FetchPlan.BASE)
                .build();

        Set<Movement> movements = movementRepository.findByCombination(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouse(),
                combination.getTransportType(),
                track,
                date,
                fetchPlan
        );

        List<PlantShipmentReason> reasons = movements.stream()
                .map(Movement::getReason)
                .filter(PlantShipmentReason.class::isInstance)
                .map(PlantShipmentReason.class::cast)
                .toList();

        plantShipmentReasonsDc.setItems(reasons);
    }

    private void loadVesselLoads(Track track,
                                 ComboKey comboKey,
                                 Terminal terminal,
                                 TransportType transportType,
                                 LocalDate date,
                                 PortBalanceMetric metric,
                                 java.util.UUID vesselId,
                                 String vesselName,
                                 boolean vesselDetail) {
        Terminal resolvedTerminal = terminal;
        TransportType resolvedTransportType = transportType;
        if (resolvedTerminal == null && comboKey != null) {
            resolvedTerminal = comboKey.terminal();
        }
        if (resolvedTransportType == null && comboKey != null) {
            resolvedTransportType = comboKey.transportType();
        }

        StringBuilder query = new StringBuilder("""
                select i from scm_VesselLoadItem i
                join i.vesselLoad v
                where 1=1
                """);
        Map<String, Object> params = new HashMap<>();
        if (track != null) {
            query.append(" and v.track = :track");
            params.put("track", track);
        }
        if (resolvedTerminal != null) {
            query.append(" and v.terminal = :terminal");
            params.put("terminal", resolvedTerminal);
        }
        if (resolvedTransportType != null) {
            query.append(" and v.transportType = :transportType");
            params.put("transportType", resolvedTransportType);
        }
        if (vesselId != null) {
            query.append(" and v.vessel.id = :vesselId");
            params.put("vesselId", vesselId);
        } else if (vesselDetail && vesselName != null) {
            query.append(" and v.vesselName = :vesselName");
            params.put("vesselName", vesselName);
        }
        query.append(" and (")
                .append("v.actualLoadingStartDate = :date ")
                .append("or v.planningLoadingStartDate = :date ")
                .append("or v.planningLaycanStartDate = :date")
                .append(")");
        params.put("date", date);

        var loader = dataManager.load(VesselLoadItem.class)
                .query(query.toString())
                .fetchPlan(fetchPlan -> fetchPlan
                        .addFetchPlan(FetchPlan.BASE)
                        .add("vesselLoad", plan -> plan
                                .addFetchPlan(FetchPlan.BASE)
                                .add("track")
                                .add("terminal", terminalPlan -> terminalPlan
                                        .add("code")
                                        .add("name"))
                                .add("transportType", transportPlan -> transportPlan.add("name"))
                                .add("planningLaycanStartDate")
                                .add("planningLaycanEndDate")
                                .add("actualLaycanStartDate")
                                .add("actualLaycanEndDate")
                                .add("planningLoadingStartDate")
                                .add("actualLoadingStartDate"))
                        .add("preParty", plan -> plan
                                .addFetchPlan(FetchPlan.BASE)
                                .add("originPlant")
                                .add("product")
                                .add("productPackage")));
        params.forEach(loader::parameter);

        List<VesselLoadItem> items = loader.list();

        boolean filterByCombo = shouldFilterOutByCombo(metric) && comboKey != null;
        List<VesselLoadItem> filtered = new ArrayList<>();
        for (VesselLoadItem item : items) {
            VesselLoad vesselLoad = item.getVesselLoad();
            if (vesselLoad == null) {
                continue;
            }
            LocalDate outDate = resolveOutDate(vesselLoad);
            if (!date.equals(outDate)) {
                continue;
            }
            if (filterByCombo && !matchesCombo(item, comboKey)) {
                continue;
            }
            filtered.add(item);
        }

        loadedVesselLoadItems = filtered;
        List<VesselLoad> vesselLoads = filtered.stream()
                .map(VesselLoadItem::getVesselLoad)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        vesselLoadsDc.setItems(vesselLoads);
        if (!vesselLoads.isEmpty()) {
            VesselLoad first = vesselLoads.get(0);
            vesselLoadGrid.select(first);
            showItemsForLoad(first);
        } else {
            vesselLoadItemsDc.setItems(List.of());
        }
    }

    private LocalDate resolveOutDate(VesselLoad vesselLoad) {
        if (vesselLoad.getActualLoadingStartDate() != null) {
            return vesselLoad.getActualLoadingStartDate();
        }
        if (vesselLoad.getPlanningLoadingStartDate() != null) {
            return vesselLoad.getPlanningLoadingStartDate();
        }
        return vesselLoad.getPlanningLaycanStartDate();
    }

    private boolean matchesCombo(VesselLoadItem item, ComboKey comboKey) {
        if (item.getPreParty() == null) {
            return false;
        }
        return Objects.equals(comboKey.plant(), item.getPreParty().getOriginPlant())
                && Objects.equals(comboKey.product(), item.getPreParty().getProduct())
                && Objects.equals(comboKey.productPackage(), item.getPreParty().getProductPackage());
    }

    private boolean shouldLoadIn(PortBalanceMetric metric) {
        return metric == PortBalanceMetric.IN || metric == PortBalanceMetric.STOCK;
    }

    private boolean shouldLoadOut(PortBalanceMetric metric) {
        return metric == PortBalanceMetric.OUT
                || metric == PortBalanceMetric.STOCK
                || metric == PortBalanceMetric.VESSEL
                || metric == PortBalanceMetric.LAYCAN
                || metric == PortBalanceMetric.TOTAL_OUT;
    }

    private boolean shouldFilterOutByCombo(PortBalanceMetric metric) {
        return metric == PortBalanceMetric.OUT || metric == PortBalanceMetric.STOCK;
    }

    private boolean requiresTrack(PortBalanceMetric metric) {
        return metric == PortBalanceMetric.IN
                || metric == PortBalanceMetric.OUT
                || metric == PortBalanceMetric.STOCK;
    }

    private void applyGridVisibility(PortBalanceMetric metric) {
        boolean showPlant = metric == PortBalanceMetric.IN || metric == PortBalanceMetric.STOCK;
        boolean showVessel = metric == PortBalanceMetric.OUT
                || metric == PortBalanceMetric.STOCK
                || metric == PortBalanceMetric.VESSEL
                || metric == PortBalanceMetric.LAYCAN
                || metric == PortBalanceMetric.TOTAL_OUT;

        setVisible(plantShipmentsLabel, showPlant);
        setVisible(plantShipmentGrid, showPlant);
        setVisible(vesselLoadItemsLabel, showVessel);
        setVisible(vesselLoadItemGrid, showVessel);
        setVisible(vesselLoadsLabel, showVessel);
        setVisible(vesselLoadGrid, showVessel);
    }

    private void setVisible(com.vaadin.flow.component.Component component, boolean visible) {
        if (component != null) {
            component.setVisible(visible);
        }
    }

    @Subscribe("vesselLoadGrid")
    public void onVesselLoadGridSelection(SelectionEvent<DataGrid<VesselLoad>, VesselLoad> event) {
        VesselLoad selected = event.getFirstSelectedItem().orElse(null);
        showItemsForLoad(selected);
    }

    private void showItemsForLoad(VesselLoad selected) {
        if (selected == null) {
            vesselLoadItemsDc.setItems(List.of());
            return;
        }
        java.util.UUID selectedId = selected.getId();
        List<VesselLoadItem> items = loadedVesselLoadItems.stream()
                .filter(item -> item.getVesselLoad() != null
                        && Objects.equals(item.getVesselLoad().getId(), selectedId))
                .toList();
        vesselLoadItemsDc.setItems(items);
    }

    private boolean isPersisted(Object entity) {
        return entity != null && EntityValues.getId(entity) != null;
    }
}
