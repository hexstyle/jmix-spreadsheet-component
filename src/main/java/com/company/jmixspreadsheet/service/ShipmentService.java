package com.company.jmixspreadsheet.service;

import com.company.jmixspreadsheet.entity.Shipment;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipmentService {

    @Autowired
    private DataManager dataManager;

    public Shipment save(Shipment shipment) {
        return dataManager.save(shipment);
    }

    public void remove(Shipment shipment) {
        dataManager.remove(shipment);
    }

    public List<Shipment> findAll() {
        return dataManager.load(Shipment.class)
                .all()
                .list();
    }

    public Shipment findById(java.util.UUID id) {
        return dataManager.load(Shipment.class)
                .id(id)
                .one();
    }
}
