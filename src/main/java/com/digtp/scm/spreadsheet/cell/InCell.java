package com.digtp.scm.spreadsheet.cell;

import io.jmix.core.entity.KeyValueEntity;
import org.apache.commons.lang3.StringUtils;

public class InCell extends DataCell<KeyValueEntity, Integer> {
    
    public InCell(KeyValueEntity in) {
        super(in, "value");
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
