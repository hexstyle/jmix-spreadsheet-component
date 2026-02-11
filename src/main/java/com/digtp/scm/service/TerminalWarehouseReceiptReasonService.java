package com.digtp.scm.service;

import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.TerminalWarehouseReceiptReason;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

@Service
public class TerminalWarehouseReceiptReasonService {
    
    private final DataManager dataManager;
    
    public TerminalWarehouseReceiptReasonService(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    public TerminalWarehouseReceiptReason create(PlantShipmentReason plantShipmentReason) {
        short leadtime = plantShipmentReason.getLeadtimeOrDefault();
        
        TerminalWarehouseReceiptReason terminalWarehouseReceiptReason = dataManager.create(TerminalWarehouseReceiptReason.class);
        terminalWarehouseReceiptReason.setOriginPlant(plantShipmentReason.getOriginPlant());
        terminalWarehouseReceiptReason.setProduct(plantShipmentReason.getProduct());
        terminalWarehouseReceiptReason.setProductPackage(plantShipmentReason.getProductPackage());
        terminalWarehouseReceiptReason.setWarehouse(plantShipmentReason.getWarehouse());
        terminalWarehouseReceiptReason.setTransportType(plantShipmentReason.getTransportType());
        terminalWarehouseReceiptReason.setTrack(plantShipmentReason.getTrack());
        terminalWarehouseReceiptReason.setDate(plantShipmentReason.getDate().plusDays(leadtime));
        terminalWarehouseReceiptReason.setVolume(plantShipmentReason.getVolume());
        terminalWarehouseReceiptReason.setParentReason(plantShipmentReason);
        return terminalWarehouseReceiptReason;
    }
}
