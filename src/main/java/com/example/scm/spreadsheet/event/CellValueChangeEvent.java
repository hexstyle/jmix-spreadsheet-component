package com.example.scm.spreadsheet.event;

import com.example.scm.spreadsheet.cell.DataCell;
import com.example.scm.spreadsheet.combination.ExPlantCombination;
import lombok.Getter;

@Getter
public class CellValueChangeEvent<T extends DataCell<E, V>, E, V> extends CellEvent<T, E, V> {
    
    private final V value;
    
    public CellValueChangeEvent(Class<T> cellType, ExPlantCombination combination, V value) {
        super(cellType, combination);
        this.value = value;
    }
}
