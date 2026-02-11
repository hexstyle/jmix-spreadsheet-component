package com.digtp.scm.spreadsheet.combination;

import com.digtp.scm.entity.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ExPlantCombination extends ExPlantGroup {
    
    private final Track track;
    private final LocalDate date;
    
    private ExPlantCombination(Plant plant,
                               Product product,
                               ProductPackage productPackage,
                               WarehouseTerminal warehouseTerminal,
                               TransportType transportType,
                               Track track,
                               LocalDate date) {
        super(plant, product, productPackage, warehouseTerminal, transportType);
        this.track = track;
        this.date = date;
    }
    
    public ExPlantCombination(ExPlantGroup group, Track track, LocalDate date) {
        this(group.getPlant(),
                group.getProduct(),
                group.getProductPackage(),
                group.getWarehouseTerminal(),
                group.getTransportType(),
                track,
                date);
    }
    
    public ExPlantCombination(PlantShipmentReason plantShipmentReason) {
        this(plantShipmentReason.getOriginPlant(),
                plantShipmentReason.getProduct(),
                plantShipmentReason.getProductPackage(),
                plantShipmentReason.getWarehouse(),
                plantShipmentReason.getTransportType(),
                plantShipmentReason.getTrack(),
                plantShipmentReason.getDate());
    }
    
    public ExPlantCombination atDate(LocalDate date) {
        return new ExPlantCombination(plant, product, productPackage, warehouseTerminal, transportType, track, date);
    }
}
