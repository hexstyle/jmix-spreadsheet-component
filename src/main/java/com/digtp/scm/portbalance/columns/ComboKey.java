package com.digtp.scm.portbalance.columns;

import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.ShippingCombination;
import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.WarehouseTerminal;

public record ComboKey(Plant plant,
                       Product product,
                       ProductPackage productPackage,
                       TransportType transportType,
                       Terminal terminal) {

    public static ComboKey from(ShippingCombination combination) {
        if (combination == null) {
            throw new IllegalArgumentException("Shipping combination is required");
        }
        WarehouseTerminal warehouseTerminal = combination.getWarehouse();
        Terminal terminal = warehouseTerminal == null ? null : warehouseTerminal.getTerminal();
        return new ComboKey(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getTransportType(),
                terminal
        );
    }
}
