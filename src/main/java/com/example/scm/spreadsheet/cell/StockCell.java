package com.example.scm.spreadsheet.cell;

import io.jmix.core.entity.KeyValueEntity;
import org.apache.commons.lang3.StringUtils;

public class StockCell extends DataCell<KeyValueEntity, Integer> {
    
    public StockCell(KeyValueEntity stock) {
        super(stock, "value");
    }
    
    @Override
    public Integer parseValue(String value) {
        value = value.replace(".", ",").replace(" ", "");
        value = StringUtils.substringBeforeLast(value, ",");
        return Integer.parseInt(value);
    }
    
    @Override
    public Integer parseValue(Number value) {
        return value.intValue();
    }
}
