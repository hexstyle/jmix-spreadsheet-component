package com.example.scm.portbalance.laycan;

import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class LaycanGroupingTest {

    private final LaycanGrouper grouper = new LaycanGrouper();

    @Test
    void totalOutIsSumOfVesselLoadItems() {
        VesselLoad load = newVesselLoad(LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                "Vessel A",
                10,
                25);

        List<VesselDayGroup> groups = grouper.group(List.of(load));

        Assertions.assertThat(groups).hasSize(1);
        VesselSummary summary = groups.get(0).getSummary();
        Assertions.assertThat(summary.getTotalOut()).isEqualTo(35);
        Assertions.assertThat(summary.getVesselLabel()).isEqualTo("Vessel A");
        Assertions.assertThat(summary.getLaycanStart()).isEqualTo(LocalDate.of(2026, 5, 1));
        Assertions.assertThat(summary.getLaycanEnd()).isEqualTo(LocalDate.of(2026, 5, 3));
        Assertions.assertThat(summary.getVesselCount()).isEqualTo(1);
    }

    @Test
    void groupsMultipleVesselsByDay() {
        VesselLoad first = newVesselLoad(LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                "Alpha",
                20);
        VesselLoad second = newVesselLoad(LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 11),
                "Beta",
                35,
                5);

        List<VesselDayGroup> groups = grouper.group(List.of(first, second));

        Assertions.assertThat(groups).hasSize(1);
        VesselSummary summary = groups.get(0).getSummary();
        Assertions.assertThat(summary.getVesselLabel()).isEqualTo("2 vessels");
        Assertions.assertThat(summary.getLaycanStart()).isNull();
        Assertions.assertThat(summary.getLaycanEnd()).isNull();
        Assertions.assertThat(summary.getTotalOut()).isEqualTo(60);
        Assertions.assertThat(summary.getVesselCount()).isEqualTo(2);
        Assertions.assertThat(groups.get(0).getDetails()).hasSize(2);
    }

    @Test
    void mergesSameVesselAcrossLoads() {
        VesselLoad first = newVesselLoad(LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "Gamma",
                10);
        VesselLoad second = newVesselLoad(LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2),
                "Gamma",
                15);

        List<VesselDayGroup> groups = grouper.group(List.of(first, second));

        Assertions.assertThat(groups).hasSize(1);
        VesselDayGroup group = groups.get(0);
        Assertions.assertThat(group.getDetails()).hasSize(1);

        VesselSummary summary = group.getSummary();
        Assertions.assertThat(summary.getVesselLabel()).isEqualTo("Gamma");
        Assertions.assertThat(summary.getVesselCount()).isEqualTo(1);
        Assertions.assertThat(summary.getTotalOut()).isEqualTo(25);
        Assertions.assertThat(summary.getLaycanStart()).isEqualTo(LocalDate.of(2026, 7, 1));
        Assertions.assertThat(summary.getLaycanEnd()).isEqualTo(LocalDate.of(2026, 7, 3));
    }

    private VesselLoad newVesselLoad(LocalDate laycanStart,
                                     LocalDate laycanEnd,
                                     String vesselName,
                                     int... itemVolumes) {
        VesselLoad load = new VesselLoad();
        load.setPlanningLaycanStartDate(laycanStart);
        load.setPlanningLaycanEndDate(laycanEnd);
        load.setVesselName(vesselName);

        List<VesselLoadItem> items = new ArrayList<>();
        for (int volume : itemVolumes) {
            VesselLoadItem item = new VesselLoadItem();
            item.setVesselLoad(load);
            item.setVolume(volume);
            items.add(item);
        }
        load.setItems(items);
        return load;
    }
}
