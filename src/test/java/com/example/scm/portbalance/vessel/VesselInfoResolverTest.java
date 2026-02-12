package com.example.scm.portbalance.vessel;

import com.example.scm.entity.Vessel;
import com.example.scm.entity.VesselLoad;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class VesselInfoResolverTest {

    private final VesselInfoResolver resolver = new VesselInfoResolver();

    @Test
    void laycanUsesActualDatesWhenPresent() {
        VesselLoad vesselLoad = baseVesselLoad();
        vesselLoad.setActualLaycanStartDate(LocalDate.of(2026, 4, 1));
        vesselLoad.setActualLaycanEndDate(LocalDate.of(2026, 4, 3));

        VesselInfo info = resolver.resolve(vesselLoad);

        Assertions.assertThat(info.getLaycanStart()).isEqualTo(LocalDate.of(2026, 4, 1));
        Assertions.assertThat(info.getLaycanEnd()).isEqualTo(LocalDate.of(2026, 4, 3));
    }

    @Test
    void laycanFallsBackToPlanningDatesWhenActualMissing() {
        VesselLoad vesselLoad = baseVesselLoad();
        vesselLoad.setActualLaycanStartDate(null);
        vesselLoad.setActualLaycanEndDate(null);

        VesselInfo info = resolver.resolve(vesselLoad);

        Assertions.assertThat(info.getLaycanStart()).isEqualTo(LocalDate.of(2026, 4, 10));
        Assertions.assertThat(info.getLaycanEnd()).isEqualTo(LocalDate.of(2026, 4, 12));
    }

    @Test
    void vesselNameUsesVesselLoadNameWhenPresent() {
        VesselLoad vesselLoad = baseVesselLoad();
        vesselLoad.setVesselName("Direct Vessel");

        VesselInfo info = resolver.resolve(vesselLoad);

        Assertions.assertThat(info.getVesselName()).isEqualTo("Direct Vessel");
    }

    @Test
    void vesselNameFallsBackToVesselEntityName() {
        VesselLoad vesselLoad = baseVesselLoad();
        vesselLoad.setVesselName(null);

        Vessel vessel = new Vessel();
        vessel.setName("Fallback Vessel");
        vessel.setImo("1234567");
        vesselLoad.setVessel(vessel);

        VesselInfo info = resolver.resolve(vesselLoad);

        Assertions.assertThat(info.getVesselName()).isEqualTo("Fallback Vessel");
    }

    private VesselLoad baseVesselLoad() {
        VesselLoad vesselLoad = new VesselLoad();
        vesselLoad.setPlanningLaycanStartDate(LocalDate.of(2026, 4, 10));
        vesselLoad.setPlanningLaycanEndDate(LocalDate.of(2026, 4, 12));
        vesselLoad.setVesselName("Planning Vessel");
        return vesselLoad;
    }
}
