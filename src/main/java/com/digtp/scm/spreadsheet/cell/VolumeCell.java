package com.digtp.scm.spreadsheet.cell;

import com.digtp.scm.entity.PlantShipmentReason;
import org.apache.commons.lang3.StringUtils;

public class VolumeCell extends DataCell<PlantShipmentReason, Integer> {
    
    public VolumeCell(PlantShipmentReason plantShipmentReason) {
        super(plantShipmentReason, "volume");
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
    
    @Override
    public boolean isValid() {
        Integer volume = getValue();
        return volume == null || volume >= 0;
    }
}
