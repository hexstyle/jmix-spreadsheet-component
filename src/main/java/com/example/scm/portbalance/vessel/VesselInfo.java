package com.example.scm.portbalance.vessel;

import java.time.LocalDate;

public class VesselInfo {

    private final LocalDate laycanStart;
    private final LocalDate laycanEnd;
    private final String vesselName;

    public VesselInfo(LocalDate laycanStart, LocalDate laycanEnd, String vesselName) {
        this.laycanStart = laycanStart;
        this.laycanEnd = laycanEnd;
        this.vesselName = vesselName;
    }

    public LocalDate getLaycanStart() {
        return laycanStart;
    }

    public LocalDate getLaycanEnd() {
        return laycanEnd;
    }

    public String getVesselName() {
        return vesselName;
    }
}
