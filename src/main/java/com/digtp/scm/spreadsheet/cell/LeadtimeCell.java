package com.digtp.scm.spreadsheet.cell;

import com.digtp.scm.entity.PlantShipmentReason;
import org.apache.commons.lang3.StringUtils;

public class LeadtimeCell extends DataCell<PlantShipmentReason, Short> {
    
    public LeadtimeCell(PlantShipmentReason plantShipmentReason) {
        super(plantShipmentReason, "leadtime");
    }
    
    @Override
    public Short parseValue(String value) {
        value = value.replace(".", ",").replace(" ", "");
        value = StringUtils.substringBeforeLast(value, ",");
        return Short.parseShort(value);
    }
    
    @Override
    public Short parseValue(Number value) {
        return value.shortValue();
    }
    
    @Override
    public boolean isValid() {
        Short leadtime = getValue();
        return leadtime == null || leadtime >= 0;
    }
}
