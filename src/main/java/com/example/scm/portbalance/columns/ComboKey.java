package com.example.scm.portbalance.columns;

import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.ShippingCombination;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.WarehouseTerminal;

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
