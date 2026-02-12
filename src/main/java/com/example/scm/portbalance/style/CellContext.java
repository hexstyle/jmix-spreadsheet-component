package com.example.scm.portbalance.style;

import com.example.scm.portbalance.columns.PortBalanceColumnKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
import com.example.scm.portbalance.rows.PortBalanceRow;

import java.time.LocalDate;

public class CellContext {

    private final PortBalanceRow row;
    private final PortBalanceColumnKey columnKey;
    private final Object value;
    private final LocalDate today;

    public CellContext(PortBalanceRow row,
                       PortBalanceColumnKey columnKey,
                       Object value,
                       LocalDate today) {
        this.row = row;
        this.columnKey = columnKey;
        this.value = value;
        this.today = today;
    }

    public PortBalanceRow getRow() {
        return row;
    }

    public PortBalanceColumnKey getColumnKey() {
        return columnKey;
    }

    public Object getValue() {
        return value;
    }

    public LocalDate getToday() {
        return today;
    }

    public boolean isMonthBreak() {
        return row != null && row.isMonthBreak();
    }

    public boolean isTodayRow() {
        if (row == null || row.getDate() == null || today == null) {
            return false;
        }
        return !isMonthBreak() && today.equals(row.getDate());
    }

    public PortBalanceMetric getMetric() {
        return columnKey == null ? null : columnKey.metric();
    }

    public boolean isNegativeStock() {
        if (getMetric() != PortBalanceMetric.STOCK) {
            return false;
        }
        if (value instanceof Number number) {
            return number.doubleValue() < 0;
        }
        return false;
    }

    public boolean isMultiVessel() {
        if (getMetric() != PortBalanceMetric.VESSEL) {
            return false;
        }
        if (value instanceof String text) {
            return text.trim().matches("\\d+\\s+vessels");
        }
        return false;
    }
}
