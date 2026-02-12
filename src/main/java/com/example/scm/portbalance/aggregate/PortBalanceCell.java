package com.example.scm.portbalance.aggregate;

import com.example.scm.portbalance.columns.PortBalanceColumnKey;

public class PortBalanceCell {

    private final String rowId;
    private final PortBalanceColumnKey columnKey;
    private final Object value;
    private final Object context;

    public PortBalanceCell(String rowId,
                           PortBalanceColumnKey columnKey,
                           Object value,
                           Object context) {
        this.rowId = rowId;
        this.columnKey = columnKey;
        this.value = value;
        this.context = context;
    }

    public String getRowId() {
        return rowId;
    }

    public PortBalanceColumnKey getColumnKey() {
        return columnKey;
    }

    public Object getValue() {
        return value;
    }

    public Object getContext() {
        return context;
    }
}
