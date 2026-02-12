package com.example.scm.portbalance.query;

import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import com.example.scm.entity.VesselLoadingReason;

import java.time.LocalDate;

public record VesselLoadingOutRecord(VesselLoadingReason reason,
                                     VesselLoadItem item,
                                     VesselLoad vesselLoad,
                                     LocalDate loadingStartDate,
                                     LocalDate laycanStart,
                                     LocalDate laycanEnd) {
}
