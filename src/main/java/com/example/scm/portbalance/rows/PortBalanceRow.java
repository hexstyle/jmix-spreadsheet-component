package com.example.scm.portbalance.rows;

import java.time.LocalDate;
public class PortBalanceRow {

    private final String rowId;
    private final LocalDate date;
    private final PortBalanceRowType type;
    private final String label;
    private final String groupId;
    private final java.util.UUID vesselId;
    private final String vesselName;

    public static PortBalanceRow monthBreak(LocalDate date, String label) {
        return new PortBalanceRow("M:" + label, date, PortBalanceRowType.MONTH_BREAK, label, null, null, null);
    }

    public static PortBalanceRow dateRow(LocalDate date) {
        return new PortBalanceRow("D:" + date, date, PortBalanceRowType.DATE, null, null, null, null);
    }

    public static PortBalanceRow vesselDetail(LocalDate date, String parentRowId, int index) {
        return vesselDetail(date, parentRowId, index, null, null);
    }

    public static PortBalanceRow vesselDetail(LocalDate date,
                                              String parentRowId,
                                              int index,
                                              java.util.UUID vesselId,
                                              String vesselName) {
        String rowId = parentRowId + ":V" + index;
        return new PortBalanceRow(rowId, date, PortBalanceRowType.VESSEL_DETAIL, null, parentRowId, vesselId, vesselName);
    }

    public PortBalanceRow(String rowId,
                          LocalDate date,
                          PortBalanceRowType type,
                          String label,
                          String groupId,
                          java.util.UUID vesselId,
                          String vesselName) {
        this.rowId = rowId;
        this.date = date;
        this.type = type;
        this.label = label;
        this.groupId = groupId;
        this.vesselId = vesselId;
        this.vesselName = vesselName;
    }

    public String getRowId() {
        return rowId;
    }

    public LocalDate getDate() {
        return date;
    }

    public PortBalanceRowType getType() {
        return type;
    }

    public boolean isMonthBreak() {
        return type == PortBalanceRowType.MONTH_BREAK;
    }

    public boolean isVesselDetail() {
        return type == PortBalanceRowType.VESSEL_DETAIL;
    }

    public String getLabel() {
        return label;
    }

    public String getGroupId() {
        return groupId;
    }

    public java.util.UUID getVesselId() {
        return vesselId;
    }

    public String getVesselName() {
        return vesselName;
    }
}
