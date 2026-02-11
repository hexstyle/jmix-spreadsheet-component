package com.digtp.scm.spreadsheet.combination;

import com.digtp.scm.entity.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ExPlantGroup implements Group {
    
    protected final Plant plant;
    protected final Product product;
    protected final ProductPackage productPackage;
    protected final WarehouseTerminal warehouseTerminal;
    protected final TransportType transportType;
    
    public String getPrimaryLabel() {
        return "%s %s %s".formatted(plant.getCode(), product.getName(), productPackage.getCode());
    }
    
    public String getSecondaryLabel() {
        return warehouseTerminal.getName();
    }
}
