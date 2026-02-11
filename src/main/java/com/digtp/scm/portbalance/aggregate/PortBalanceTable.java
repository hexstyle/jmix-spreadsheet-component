package com.digtp.scm.portbalance.aggregate;

import com.digtp.scm.portbalance.columns.PortBalanceColumnKey;
import com.digtp.scm.portbalance.rows.PortBalanceRow;

import java.util.List;

public class PortBalanceTable {

    private final List<PortBalanceRow> rows;
    private final List<PortBalanceColumnKey> columns;
    private final List<PortBalanceCell> cells;

    public PortBalanceTable(List<PortBalanceRow> rows,
                            List<PortBalanceColumnKey> columns,
                            List<PortBalanceCell> cells) {
        this.rows = rows;
        this.columns = columns;
        this.cells = cells;
    }

    public List<PortBalanceRow> getRows() {
        return rows;
    }

    public List<PortBalanceColumnKey> getColumns() {
        return columns;
    }

    public List<PortBalanceCell> getCells() {
        return cells;
    }
}
