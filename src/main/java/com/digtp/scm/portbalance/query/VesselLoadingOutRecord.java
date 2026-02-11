package com.digtp.scm.portbalance.query;

import com.digtp.scm.entity.VesselLoad;
import com.digtp.scm.entity.VesselLoadItem;
import com.digtp.scm.entity.VesselLoadingReason;

import java.time.LocalDate;

public record VesselLoadingOutRecord(VesselLoadingReason reason,
                                     VesselLoadItem item,
                                     VesselLoad vesselLoad,
                                     LocalDate loadingStartDate,
                                     LocalDate laycanStart,
                                     LocalDate laycanEnd) {
}
