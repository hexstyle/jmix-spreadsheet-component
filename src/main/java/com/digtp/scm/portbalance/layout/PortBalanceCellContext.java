package com.digtp.scm.portbalance.layout;

import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.portbalance.columns.ComboKey;
import com.digtp.scm.portbalance.columns.PortBalanceMetric;
import com.digtp.scm.portbalance.columns.TrackKey;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;

import java.time.LocalDate;
public class PortBalanceCellContext implements CellBinding.PivotContext {

    private final LocalDate date;
    private final TrackKey trackKey;
    private final Terminal terminal;
    private final TransportType transportType;
    private final PortBalanceMetric metric;
    private final String vessel;
    private final ComboKey comboKey;
    private final boolean monthBreak;
    private final boolean vesselDetail;
    private final java.util.UUID vesselId;
    private final String vesselName;

    public PortBalanceCellContext(LocalDate date,
                                  TrackKey trackKey,
                                  Terminal terminal,
                                  TransportType transportType,
                                  PortBalanceMetric metric,
                                  String vessel,
                                  ComboKey comboKey,
                                  boolean monthBreak,
                                  boolean vesselDetail,
                                  java.util.UUID vesselId,
                                  String vesselName) {
        this.date = date;
        this.trackKey = trackKey;
        this.terminal = terminal;
        this.transportType = transportType;
        this.metric = metric;
        this.vessel = vessel;
        this.comboKey = comboKey;
        this.monthBreak = monthBreak;
        this.vesselDetail = vesselDetail;
        this.vesselId = vesselId;
        this.vesselName = vesselName;
    }

    public LocalDate getDate() {
        return date;
    }

    public TrackKey getTrackKey() {
        return trackKey;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public PortBalanceMetric getMetric() {
        return metric;
    }

    public String getVessel() {
        return vessel;
    }

    public ComboKey getComboKey() {
        return comboKey;
    }

    public boolean isMonthBreak() {
        return monthBreak;
    }

    public boolean isVesselDetail() {
        return vesselDetail;
    }

    public java.util.UUID getVesselId() {
        return vesselId;
    }

    public String getVesselName() {
        return vesselName;
    }
}
