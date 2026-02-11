package com.digtp.scm.portbalance.laycan;

import java.time.LocalDate;
import java.util.List;

public class VesselDayGroup {

    private final LocalDate date;
    private final List<VesselDetail> details;

    public VesselDayGroup(LocalDate date, List<VesselDetail> details) {
        this.date = date;
        this.details = details;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<VesselDetail> getDetails() {
        return details;
    }

    public boolean isMultiVessel() {
        return details != null && details.size() > 1;
    }

    public VesselSummary getSummary() {
        if (details == null || details.isEmpty()) {
            return null;
        }
        if (details.size() == 1) {
            VesselDetail detail = details.get(0);
            return new VesselSummary(
                    date,
                    detail.getVesselName(),
                    detail.getLaycanStart(),
                    detail.getLaycanEnd(),
                    detail.getTotalOut(),
                    1
            );
        }
        int totalOut = details.stream()
                .mapToInt(VesselDetail::getTotalOut)
                .sum();
        return new VesselSummary(
                date,
                details.size() + " vessels",
                null,
                null,
                totalOut,
                details.size()
        );
    }
}
