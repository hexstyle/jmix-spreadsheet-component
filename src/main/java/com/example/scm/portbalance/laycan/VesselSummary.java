package com.example.scm.portbalance.laycan;

import java.time.LocalDate;

public class VesselSummary {

    private final LocalDate date;
    private final String vesselLabel;
    private final LocalDate laycanStart;
    private final LocalDate laycanEnd;
    private final int totalOut;
    private final int vesselCount;

    public VesselSummary(LocalDate date,
                         String vesselLabel,
                         LocalDate laycanStart,
                         LocalDate laycanEnd,
                         int totalOut,
                         int vesselCount) {
        this.date = date;
        this.vesselLabel = vesselLabel;
        this.laycanStart = laycanStart;
        this.laycanEnd = laycanEnd;
        this.totalOut = totalOut;
        this.vesselCount = vesselCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getVesselLabel() {
        return vesselLabel;
    }

    public LocalDate getLaycanStart() {
        return laycanStart;
    }

    public LocalDate getLaycanEnd() {
        return laycanEnd;
    }

    public int getTotalOut() {
        return totalOut;
    }

    public int getVesselCount() {
        return vesselCount;
    }
}
