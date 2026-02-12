package com.example.scm.spreadsheet.event;

import com.example.scm.spreadsheet.cell.DataCell;
import com.example.scm.spreadsheet.combination.ExPlantCombination;
import lombok.Getter;

@Getter
public class CellReadOnlyChangeEvent<T extends DataCell<E, V>, E, V> extends CellEvent<T, E, V> {
    
    private final boolean readOnly;
    
    public CellReadOnlyChangeEvent(Class<T> cellType, ExPlantCombination combination, boolean readOnly) {
        super(cellType, combination);
        this.readOnly = readOnly;
    }
}
