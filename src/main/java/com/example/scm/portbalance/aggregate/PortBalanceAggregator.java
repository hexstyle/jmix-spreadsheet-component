package com.example.scm.portbalance.aggregate;

import com.example.scm.entity.Movement;
import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.ShippingCombination;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.Track;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import com.example.scm.entity.WarehouseTerminal;
import com.example.scm.portbalance.columns.ComboKey;
import com.example.scm.portbalance.columns.PortBalanceColumnKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
import com.example.scm.portbalance.columns.TrackKey;
import com.example.scm.portbalance.laycan.LaycanGrouper;
import com.example.scm.portbalance.laycan.VesselDayGroup;
import com.example.scm.portbalance.laycan.VesselDetail;
import com.example.scm.portbalance.laycan.VesselSummary;
import com.example.scm.portbalance.rows.DateAxisBuilder;
import com.example.scm.portbalance.rows.PortBalanceRow;
import com.example.scm.repository.MovementRepository;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PortBalanceAggregator {

    private final MovementRepository movementRepository;
    private final DataManager dataManager;
    private final DateAxisBuilder dateAxisBuilder = new DateAxisBuilder();
    private final LaycanGrouper laycanGrouper = new LaycanGrouper();

    public PortBalanceAggregator(MovementRepository movementRepository, DataManager dataManager) {
        this.movementRepository = movementRepository;
        this.dataManager = dataManager;
    }

    public PortBalanceTable aggregate(List<ShippingCombination> combinations,
                                      List<Track> tracks,
                                      LocalDate from,
                                      LocalDate to) {
        List<PortBalanceRow> baseRows = dateAxisBuilder.build(from, to);
        List<PortBalanceColumnKey> columns = buildColumns(combinations, tracks);
        List<PortBalanceCell> cells = new ArrayList<>();

        Set<Terminal> terminals = resolveTerminals(combinations);
        List<VesselLoad> vesselLoads = loadVesselLoads(tracks, terminals);
        List<VesselDayGroup> vesselDayGroups = laycanGrouper.group(vesselLoads);
        Map<LocalDate, VesselDayGroup> vesselGroupsByDate = vesselDayGroups.stream()
                .filter(group -> group.getDate() != null)
                .filter(group -> !group.getDate().isBefore(from) && !group.getDate().isAfter(to))
                .collect(Collectors.toMap(VesselDayGroup::getDate, group -> group, (a, b) -> a));

        Map<VesselLoad, Map<ComboKey, Integer>> outByCombo = buildOutByCombo(vesselLoads);
        Map<TrackDateKey, Map<ComboKey, Integer>> outByTrackDate =
                buildOutByTrackDate(vesselLoads, outByCombo);

        RowExpansion expansion = expandRows(baseRows, vesselGroupsByDate);
        List<PortBalanceRow> rows = expansion.rows;
        Map<String, VesselDetail> vesselDetailByRowId = expansion.detailsByRowId;

        PortBalanceColumnKey vesselColumn = PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL);
        PortBalanceColumnKey laycanColumn = PortBalanceColumnKey.of(null, null, PortBalanceMetric.LAYCAN);
        PortBalanceColumnKey totalOutColumn = PortBalanceColumnKey.of(null, null, PortBalanceMetric.TOTAL_OUT);

        Map<YearMonth, Integer> monthTotalOut = new HashMap<>();
        for (PortBalanceRow row : rows) {
            if (row.isMonthBreak() || row.isVesselDetail()) {
                continue;
            }
            VesselDayGroup group = vesselGroupsByDate.get(row.getDate());
            if (group != null) {
                VesselSummary summary = group.getSummary();
                if (summary != null) {
                    monthTotalOut.merge(YearMonth.from(row.getDate()), summary.getTotalOut(), Integer::sum);
                }
            }
        }

        for (PortBalanceRow row : rows) {
            String rowId = row.getRowId();
            if (row.isMonthBreak()) {
                YearMonth month = YearMonth.from(row.getDate());
                addCell(cells, rowId, totalOutColumn, monthTotalOut.getOrDefault(month, 0));
                continue;
            }
            if (row.isVesselDetail()) {
                VesselDetail detail = vesselDetailByRowId.get(rowId);
                if (detail != null) {
                    addCell(cells, rowId, vesselColumn, detail.getVesselName());
                    addCell(cells, rowId, laycanColumn, formatLaycan(detail.getLaycanStart(), detail.getLaycanEnd()));
                    addCell(cells, rowId, totalOutColumn, detail.getTotalOut());
                }
                continue;
            }
            VesselDayGroup group = vesselGroupsByDate.get(row.getDate());
            if (group != null) {
                VesselSummary summary = group.getSummary();
                if (summary != null) {
                    addCell(cells, rowId, vesselColumn, summary.getVesselLabel());
                    addCell(cells, rowId, laycanColumn, formatLaycan(summary.getLaycanStart(), summary.getLaycanEnd()));
                    addCell(cells, rowId, totalOutColumn, summary.getTotalOut());
                }
            }
        }

        for (ShippingCombination combination : combinations) {
            ComboKey comboKey = ComboKey.from(combination);
            for (Track track : tracks) {
                Map<LocalDate, Integer> inByDate = loadInMovements(combination, List.of(track), from, to);
                OutMovements outMovements = loadOutMovements(combination, track, from, to);
                Map<LocalDate, Integer> outByDate =
                        resolveOutByDate(comboKey, track, outMovements, outByTrackDate, from, to);

                Map<YearMonth, Integer> monthIn = new HashMap<>();
                Map<YearMonth, Integer> monthOut = new HashMap<>();

                for (PortBalanceRow row : rows) {
                    if (row.isMonthBreak() || row.isVesselDetail()) {
                        continue;
                    }
                    LocalDate date = row.getDate();
                    YearMonth month = YearMonth.from(date);
                    monthIn.merge(month, inByDate.getOrDefault(date, 0), Integer::sum);
                    monthOut.merge(month, outByDate.getOrDefault(date, 0), Integer::sum);
                }

                int stock = 0;
                for (PortBalanceRow row : rows) {
                    String rowId = row.getRowId();
                    if (row.isMonthBreak()) {
                        YearMonth month = YearMonth.from(row.getDate());
                        addCell(cells, rowId, combination, track, PortBalanceMetric.IN,
                                monthIn.getOrDefault(month, 0));
                        addCell(cells, rowId, combination, track, PortBalanceMetric.OUT,
                                monthOut.getOrDefault(month, 0));
                        continue;
                    }

                if (row.isVesselDetail()) {
                    VesselDetail detail = vesselDetailByRowId.get(rowId);
                    if (detail == null) {
                        continue;
                    }
                    int outVolume = outFromDetail(detail, comboKey, track, outByCombo);
                    if (outVolume != 0) {
                        addCell(cells, rowId, combination, track, PortBalanceMetric.OUT, outVolume);
                    }
                    continue;
                    }

                    LocalDate date = row.getDate();
                    int inVolume = inByDate.getOrDefault(date, 0);
                    int outVolume = outByDate.getOrDefault(date, 0);
                    stock += inVolume - outVolume;

                    addCell(cells, rowId, combination, track, PortBalanceMetric.IN, inVolume);
                    addCell(cells, rowId, combination, track, PortBalanceMetric.OUT, outVolume);
                    addCell(cells, rowId, combination, track, PortBalanceMetric.STOCK, stock);
                }
            }
        }

        return new PortBalanceTable(rows, columns, cells);
    }

    private List<PortBalanceColumnKey> buildColumns(List<ShippingCombination> combinations, List<Track> tracks) {
        List<PortBalanceColumnKey> columns = new ArrayList<>();
        columns.add(PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL));
        columns.add(PortBalanceColumnKey.of(null, null, PortBalanceMetric.LAYCAN));
        columns.add(PortBalanceColumnKey.of(null, null, PortBalanceMetric.TOTAL_OUT));

        List<PortBalanceMetric> metrics = List.of(
                PortBalanceMetric.IN,
                PortBalanceMetric.OUT,
                PortBalanceMetric.STOCK
        );
        for (ShippingCombination combination : combinations) {
            for (Track track : tracks) {
                for (PortBalanceMetric metric : metrics) {
                    columns.add(PortBalanceColumnKey.from(combination, track, metric));
                }
            }
        }
        return columns;
    }

    private Set<Terminal> resolveTerminals(List<ShippingCombination> combinations) {
        Set<Terminal> terminals = new HashSet<>();
        for (ShippingCombination combination : combinations) {
            WarehouseTerminal warehouse = combination.getWarehouse();
            if (warehouse != null && warehouse.getTerminal() != null) {
                terminals.add(warehouse.getTerminal());
            }
        }
        return terminals;
    }

    private RowExpansion expandRows(List<PortBalanceRow> baseRows,
                                    Map<LocalDate, VesselDayGroup> vesselGroupsByDate) {
        List<PortBalanceRow> rows = new ArrayList<>();
        Map<String, VesselDetail> detailsByRowId = new HashMap<>();

        for (PortBalanceRow row : baseRows) {
            rows.add(row);
            if (row.isMonthBreak()) {
                continue;
            }
            VesselDayGroup group = vesselGroupsByDate.get(row.getDate());
            if (group == null || !group.isMultiVessel()) {
                continue;
            }
            int index = 1;
            for (VesselDetail detail : group.getDetails()) {
                PortBalanceRow detailRow = PortBalanceRow.vesselDetail(
                        row.getDate(),
                        row.getRowId(),
                        index++,
                        detail.getVesselId(),
                        detail.getVesselName()
                );
                rows.add(detailRow);
                detailsByRowId.put(detailRow.getRowId(), detail);
            }
        }

        return new RowExpansion(rows, detailsByRowId);
    }

    private Map<LocalDate, Integer> loadInMovements(ShippingCombination combination,
                                                    Collection<Track> tracks,
                                                    LocalDate from,
                                                    LocalDate to) {
        Set<Movement> movements = movementRepository.findByCombinationAndDateBetween(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouse(),
                combination.getTransportType(),
                tracks,
                from,
                to
        );
        Map<LocalDate, Integer> volumes = new HashMap<>();
        for (Movement movement : movements) {
            volumes.merge(movement.getDate(), movement.getVolume(), Integer::sum);
        }
        return volumes;
    }

    private OutMovements loadOutMovements(ShippingCombination combination,
                                          Track track,
                                          LocalDate from,
                                          LocalDate to) {
        Set<Movement> movements = movementRepository.findVesselLoadingByCombinationAndDateBetween(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouse(),
                combination.getTransportType(),
                track,
                from,
                to
        );
        Map<LocalDate, Integer> volumes = new HashMap<>();
        Map<LocalDate, Integer> counts = new HashMap<>();
        for (Movement movement : movements) {
            volumes.merge(movement.getDate(), movement.getVolume(), Integer::sum);
            counts.merge(movement.getDate(), 1, Integer::sum);
        }
        return new OutMovements(volumes, counts);
    }

    private Map<LocalDate, Integer> resolveOutByDate(ComboKey comboKey,
                                                     Track track,
                                                     OutMovements outMovements,
                                                     Map<TrackDateKey, Map<ComboKey, Integer>> outByTrackDate,
                                                     LocalDate from,
                                                     LocalDate to) {
        Map<LocalDate, Integer> outByDate = new HashMap<>();
        TrackKey trackKey = TrackKey.from(track);
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            int movementCount = outMovements.counts.getOrDefault(cursor, 0);
            int volume;
            if (movementCount > 0) {
                volume = outMovements.volumes.getOrDefault(cursor, 0);
            } else {
                Map<ComboKey, Integer> byCombo = outByTrackDate
                        .getOrDefault(new TrackDateKey(trackKey, cursor), Map.of());
                volume = byCombo.getOrDefault(comboKey, 0);
            }
            outByDate.put(cursor, volume);
            cursor = cursor.plusDays(1);
        }
        return outByDate;
    }

    private int outFromDetail(VesselDetail detail,
                              ComboKey comboKey,
                              Track track,
                              Map<VesselLoad, Map<ComboKey, Integer>> outByCombo) {
        if (detail == null || detail.getVesselLoads() == null || track == null) {
            return 0;
        }
        int total = 0;
        for (VesselLoad vesselLoad : detail.getVesselLoads()) {
            if (vesselLoad == null || vesselLoad.getTrack() == null) {
                continue;
            }
            TrackKey detailTrackKey = TrackKey.from(vesselLoad.getTrack());
            if (!Objects.equals(detailTrackKey, TrackKey.from(track))) {
                continue;
            }
            Terminal terminal = vesselLoad.getTerminal();
            TransportType transportType = vesselLoad.getTransportType();
            if (!Objects.equals(comboKey.terminal(), terminal)) {
                continue;
            }
            if (!Objects.equals(comboKey.transportType(), transportType)) {
                continue;
            }
            Map<ComboKey, Integer> byCombo = outByCombo.getOrDefault(vesselLoad, Map.of());
            total += byCombo.getOrDefault(comboKey, 0);
        }
        return total;
    }

    private Map<VesselLoad, Map<ComboKey, Integer>> buildOutByCombo(List<VesselLoad> vesselLoads) {
        Map<VesselLoad, Map<ComboKey, Integer>> result = new HashMap<>();
        for (VesselLoad load : vesselLoads) {
            Map<ComboKey, Integer> byCombo = new HashMap<>();
            if (load.getItems() != null) {
                for (VesselLoadItem item : load.getItems()) {
                    if (item.getPreParty() == null) {
                        continue;
                    }
                    Plant plant = item.getPreParty().getOriginPlant();
                    Product product = item.getPreParty().getProduct();
                    ProductPackage productPackage = item.getPreParty().getProductPackage();
                    ComboKey comboKey = new ComboKey(plant, product, productPackage,
                            load.getTransportType(), load.getTerminal());
                    if (item.getVolume() != null) {
                        byCombo.merge(comboKey, item.getVolume(), Integer::sum);
                    }
                }
            }
            result.put(load, byCombo);
        }
        return result;
    }

    private Map<TrackDateKey, Map<ComboKey, Integer>> buildOutByTrackDate(
            List<VesselLoad> vesselLoads,
            Map<VesselLoad, Map<ComboKey, Integer>> outByCombo) {
        Map<TrackDateKey, Map<ComboKey, Integer>> result = new HashMap<>();
        for (VesselLoad load : vesselLoads) {
            LocalDate outDate = resolveOutDate(load);
            if (outDate == null) {
                continue;
            }
            Track track = load.getTrack();
            if (track == null) {
                continue;
            }
            TrackDateKey key = new TrackDateKey(TrackKey.from(track), outDate);
            Map<ComboKey, Integer> byCombo = result.computeIfAbsent(key, k -> new HashMap<>());
            Map<ComboKey, Integer> loadCombos = outByCombo.getOrDefault(load, Map.of());
            for (Map.Entry<ComboKey, Integer> entry : loadCombos.entrySet()) {
                byCombo.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return result;
    }

    private List<VesselLoad> loadVesselLoads(Collection<Track> tracks, Collection<Terminal> terminals) {
        if (tracks == null || tracks.isEmpty() || terminals == null || terminals.isEmpty()) {
            return List.of();
        }
        return dataManager.load(VesselLoad.class)
                .query("select v from scm_VesselLoad v where v.track in :tracks and v.terminal in :terminals")
                .parameter("tracks", tracks)
                .parameter("terminals", terminals)
                .fetchPlan(fetchPlan -> fetchPlan
                        .add("track", trackPlan -> trackPlan
                                .add("name")
                                .add("trackType"))
                        .add("terminal")
                        .add("transportType")
                        .add("vesselName")
                        .add("vessel", vesselPlan -> vesselPlan.add("name"))
                        .add("actualLoadingStartDate")
                        .add("planningLoadingStartDate")
                        .add("planningLaycanStartDate")
                        .add("actualLaycanStartDate")
                        .add("actualLaycanEndDate")
                        .add("planningLaycanEndDate")
                        .add("items", itemPlan -> itemPlan
                                .add("volume")
                                .add("preParty", prePartyPlan -> prePartyPlan
                                        .add("originPlant")
                                        .add("product")
                                        .add("productPackage"))))
                .list();
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

    private String formatLaycan(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return null;
        }
        return start + " - " + end;
    }

    private void addCell(List<PortBalanceCell> cells,
                         String rowId,
                         ShippingCombination combination,
                         Track track,
                         PortBalanceMetric metric,
                         Object value) {
        PortBalanceColumnKey key = PortBalanceColumnKey.from(combination, track, metric);
        cells.add(new PortBalanceCell(rowId, key, value, null));
    }

    private void addCell(List<PortBalanceCell> cells,
                         String rowId,
                         PortBalanceColumnKey key,
                         Object value) {
        cells.add(new PortBalanceCell(rowId, key, value, null));
    }

    private record RowExpansion(List<PortBalanceRow> rows,
                                Map<String, VesselDetail> detailsByRowId) {
    }

    private record TrackDateKey(TrackKey trackKey, LocalDate date) {
    }

    private record OutMovements(Map<LocalDate, Integer> volumes, Map<LocalDate, Integer> counts) {
    }
}
