package com.digtp.scm.spreadsheet.event;

import com.digtp.scm.spreadsheet.cell.DataCell;
import com.digtp.scm.spreadsheet.combination.ExPlantCombination;
import lombok.Getter;

@Getter
public class CellValueChangeEvent<T extends DataCell<E, V>, E, V> extends CellEvent<T, E, V> {
    
    private final V value;
    
    public CellValueChangeEvent(Class<T> cellType, ExPlantCombination combination, V value) {
        super(cellType, combination);
        this.value = value;
    }
}
