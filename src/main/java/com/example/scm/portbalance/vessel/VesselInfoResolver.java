package com.example.scm.portbalance.vessel;

import com.example.scm.entity.Vessel;
import com.example.scm.entity.VesselLoad;

import java.time.LocalDate;

public class VesselInfoResolver {

    public VesselInfo resolve(VesselLoad vesselLoad) {
        if (vesselLoad == null) {
            throw new IllegalArgumentException("Vessel load is required");
        }

        LocalDate laycanStart = vesselLoad.getActualLaycanStartDate();
        if (laycanStart == null) {
            laycanStart = vesselLoad.getPlanningLaycanStartDate();
        }

        LocalDate laycanEnd = vesselLoad.getActualLaycanEndDate();
        if (laycanEnd == null) {
            laycanEnd = vesselLoad.getPlanningLaycanEndDate();
        }

        String vesselName = vesselLoad.getVesselName();
        if (vesselName == null) {
            Vessel vessel = vesselLoad.getVessel();
            if (vessel != null) {
                vesselName = vessel.getName();
            }
        }

        return new VesselInfo(laycanStart, laycanEnd, vesselName);
    }
}
