package com.example.scm.portbalance.laycan;

import com.example.scm.entity.VesselLoad;

import java.time.LocalDate;
import java.util.List;

public class VesselDetail {

    private final VesselKey vesselKey;
    private final List<VesselLoad> vesselLoads;
    private final LocalDate laycanStart;
    private final LocalDate laycanEnd;
    private final int totalOut;

    public VesselDetail(VesselKey vesselKey,
                        List<VesselLoad> vesselLoads,
                        LocalDate laycanStart,
                        LocalDate laycanEnd,
                        int totalOut) {
        this.vesselKey = vesselKey;
        this.vesselLoads = vesselLoads;
        this.laycanStart = laycanStart;
        this.laycanEnd = laycanEnd;
        this.totalOut = totalOut;
    }

    public VesselKey getVesselKey() {
        return vesselKey;
    }

    public String getVesselName() {
        return vesselKey == null ? null : vesselKey.getVesselName();
    }

    public java.util.UUID getVesselId() {
        return vesselKey == null ? null : vesselKey.getVesselId();
    }

    public List<VesselLoad> getVesselLoads() {
        return vesselLoads;
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
}
