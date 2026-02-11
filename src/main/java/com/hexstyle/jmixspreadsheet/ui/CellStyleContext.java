package com.hexstyle.jmixspreadsheet.ui;

import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;

public final class CellStyleContext<E> {

    private final SpreadsheetLayout<E> layout;
    private final CellBinding<E> binding;

    public CellStyleContext(SpreadsheetLayout<E> layout, CellBinding<E> binding) {
        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null");
        }
        if (binding == null) {
            throw new IllegalArgumentException("Binding cannot be null");
        }
        this.layout = layout;
        this.binding = binding;
    }

    public SpreadsheetLayout<E> getLayout() {
        return layout;
    }

    public CellBinding<E> getBinding() {
        return binding;
    }

    public int getRowIndex() {
        return binding.getRowIndex();
    }

    public int getColumnIndex() {
        return binding.getColumnIndex();
    }

    public Object getValue() {
        return binding.getValue();
    }

    public E getEntityRef() {
        return binding.getEntityRef();
    }

    public CellBinding.PivotContext getPivotContext() {
        return binding.getPivotContext();
    }
}
