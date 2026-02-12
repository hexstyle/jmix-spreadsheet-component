package com.example.scm.portbalance.layout;

import com.example.scm.portbalance.aggregate.PortBalanceCell;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;

public class PortBalanceCellBinding implements CellBinding<PortBalanceCell> {

    private final int rowIndex;
    private final int columnIndex;
    private final Object value;
    private final String style;
    private final PortBalanceCell entityRef;
    private final PortBalanceCellContext context;

    public PortBalanceCellBinding(int rowIndex,
                                  int columnIndex,
                                  Object value,
                                  String style,
                                  PortBalanceCell entityRef,
                                  PortBalanceCellContext context) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.value = value;
        this.style = style;
        this.entityRef = entityRef;
        this.context = context;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public int getColumnIndex() {
        return columnIndex;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public PortBalanceCell getEntityRef() {
        return entityRef;
    }

    @Override
    public PivotContext getPivotContext() {
        return context;
    }
}
