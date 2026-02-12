package com.example.scm.portbalance.laycan;

import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import com.example.scm.portbalance.vessel.VesselInfo;
import com.example.scm.portbalance.vessel.VesselInfoResolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaycanGrouper {

    private final VesselInfoResolver vesselInfoResolver;

    public LaycanGrouper() {
        this(new VesselInfoResolver());
    }

    public LaycanGrouper(VesselInfoResolver vesselInfoResolver) {
        this.vesselInfoResolver = vesselInfoResolver;
    }

    public List<VesselDayGroup> group(List<VesselLoad> vesselLoads) {
        if (vesselLoads == null) {
            throw new IllegalArgumentException("Vessel loads are required");
        }

        Map<LocalDate, Map<VesselKey, VesselDetailBuilder>> byDate = new HashMap<>();
        for (VesselLoad vesselLoad : vesselLoads) {
            VesselInfo info = vesselInfoResolver.resolve(vesselLoad);
            LocalDate outDate = resolveOutDate(vesselLoad);
            VesselKey vesselKey = VesselKey.from(vesselLoad, info.getVesselName());
            Map<VesselKey, VesselDetailBuilder> byVessel =
                    byDate.computeIfAbsent(outDate, key -> new HashMap<>());
            VesselDetailBuilder builder = byVessel.computeIfAbsent(vesselKey, key -> new VesselDetailBuilder(vesselKey));
            builder.add(vesselLoad, info, totalOut(vesselLoad));
        }

        List<VesselDayGroup> result = new ArrayList<>();
        byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(entry -> {
                    List<VesselDetail> details = entry.getValue().values().stream()
                            .map(VesselDetailBuilder::build)
                            .sorted(Comparator.comparing(VesselDetail::getVesselName,
                                    Comparator.nullsLast(String::compareToIgnoreCase)))
                            .toList();
                    result.add(new VesselDayGroup(entry.getKey(), details));
                });
        return result;
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

    private int totalOut(VesselLoad vesselLoad) {
        if (vesselLoad.getItems() == null) {
            return 0;
        }
        return vesselLoad.getItems().stream()
                .map(VesselLoadItem::getVolume)
                .filter(volume -> volume != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static final class VesselDetailBuilder {
        private final VesselKey vesselKey;
        private final List<VesselLoad> vesselLoads = new ArrayList<>();
        private LocalDate laycanStart;
        private LocalDate laycanEnd;
        private int totalOut;

        private VesselDetailBuilder(VesselKey vesselKey) {
            this.vesselKey = vesselKey;
        }

        private void add(VesselLoad vesselLoad, VesselInfo info, int outVolume) {
            vesselLoads.add(vesselLoad);
            totalOut += outVolume;

            if (info != null) {
                LocalDate start = info.getLaycanStart();
                if (start != null && (laycanStart == null || start.isBefore(laycanStart))) {
                    laycanStart = start;
                }
                LocalDate end = info.getLaycanEnd();
                if (end != null && (laycanEnd == null || end.isAfter(laycanEnd))) {
                    laycanEnd = end;
                }
            }
        }

        private VesselDetail build() {
            return new VesselDetail(vesselKey, vesselLoads, laycanStart, laycanEnd, totalOut);
        }
    }
}
