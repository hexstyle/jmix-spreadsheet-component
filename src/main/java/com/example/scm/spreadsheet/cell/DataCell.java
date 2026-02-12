package com.example.scm.spreadsheet.cell;

import io.jmix.core.entity.EntityValues;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Association between {@link Cell} and entity property in ExPlantSheet
 * @param <E> type of entity
 * @param <V> type of entity property
 */
@Data
public abstract class DataCell<E, V> {
    
    private E entity;
    private final String property;
    
    public DataCell(E entity, String property) {
        this.entity = entity;
        this.property = property;
    }
    
    public abstract V parseValue(String value);
    
    public abstract V parseValue(Number value);
    
    public final V getValue() {
        if (entity == null) {
            return null;
        }
        return EntityValues.getValue(entity, property);
    }
    
    public final void setValue(V value) {
        EntityValues.setValue(entity, property, value);
    }
    
    public void setEntity(E entity) {
        this.entity = entity;
    }
    
    public boolean isValid() {
        return true;
    }
}
