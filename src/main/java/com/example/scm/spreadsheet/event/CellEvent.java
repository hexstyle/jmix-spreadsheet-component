package com.example.scm.spreadsheet.event;

import com.example.scm.spreadsheet.cell.DataCell;
import com.example.scm.spreadsheet.combination.ExPlantCombination;
import lombok.Getter;

@Getter
public abstract class CellEvent<T extends DataCell<E, V>, E, V> {
    
    private final Class<T> cellType;
    private final ExPlantCombination combination;
    
    protected CellEvent(Class<T> cellType, ExPlantCombination combination) {
        this.combination = combination;
        this.cellType = cellType;
    }
}
