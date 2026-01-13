package com.company.jmixspreadsheet.service;

import com.company.jmixspreadsheet.entity.Shipment;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for editing shipments via DataManager.
 * <p>
 * This service provides edit methods that work with Jmix DataManager
 * and return information about affected rows, which can be used by
 * the spreadsheet's diff mechanism to update only the affected cells.
 */
@Service
public class ShipmentDataManagerService {

    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private EntityStates entityStates;

    /**
     * Updates a shipment property via DataManager.
     * <p>
     * This method saves the shipment using DataManager, updates the specified property,
     * and returns information about the affected shipment.
     *
     * @param shipment the shipment to update
     * @param propertyName the name of the property to update
     * @param propertyValue the new value for the property
     * @return the edit result containing affected shipment information
     * @throws IllegalArgumentException if the shipment or property is invalid
     */
    public ShipmentEditResult updateProperty(Shipment shipment, 
                                             String propertyName, Object propertyValue) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null");
        }
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property name cannot be null or empty");
        }

        // Track if this is a new entity
        boolean wasNew = entityStates.isNew(shipment);

        // Update the property
        io.jmix.core.entity.EntityValues.setValue(shipment, propertyName, propertyValue);

        // Save using DataManager
        Shipment savedShipment = dataManager.save(shipment);

        // Get the entity ID
        UUID shipmentId = savedShipment.getId();
        if (shipmentId == null) {
            // This shouldn't happen, but handle it gracefully
            return ShipmentEditResult.mixed(Collections.emptySet(), wasNew);
        }

        // Return result based on whether it was a new entity
        if (wasNew) {
            return ShipmentEditResult.created(shipmentId);
        } else {
            return ShipmentEditResult.existing(Set.of(shipmentId));
        }
    }

    /**
     * Updates multiple shipment properties via DataManager.
     * <p>
     * This method saves the shipment using DataManager, updates all specified properties,
     * and returns information about the affected shipment.
     *
     * @param shipment the shipment to update
     * @param propertyUpdates a map of property names to new values
     * @return the edit result containing affected shipment information
     * @throws IllegalArgumentException if the shipment or property updates are invalid
     */
    public ShipmentEditResult updateProperties(Shipment shipment, 
                                               Map<String, Object> propertyUpdates) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null");
        }
        if (propertyUpdates == null || propertyUpdates.isEmpty()) {
            throw new IllegalArgumentException("Property updates cannot be null or empty");
        }

        // Track if this is a new entity
        boolean wasNew = entityStates.isNew(shipment);

        // Update all properties
        for (Map.Entry<String, Object> entry : propertyUpdates.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            io.jmix.core.entity.EntityValues.setValue(shipment, propertyName, propertyValue);
        }

        // Save using DataManager
        Shipment savedShipment = dataManager.save(shipment);

        // Get the entity ID
        UUID shipmentId = savedShipment.getId();
        if (shipmentId == null) {
            return ShipmentEditResult.mixed(Collections.emptySet(), wasNew);
        }

        // Return result based on whether it was a new entity
        if (wasNew) {
            return ShipmentEditResult.created(shipmentId);
        } else {
            return ShipmentEditResult.existing(Set.of(shipmentId));
        }
    }

    /**
     * Updates the value of a shipment via DataManager with running total calculation.
     * <p>
     * This method:
     * 1. Updates the shipment's value using DataManager
     * 2. Finds all subsequent shipments (day > edited shipment's day) with same plant/product/vessel
     * 3. Recalculates running totals for each subsequent shipment
     * 4. Returns all affected entities for the change analyzer
     *
     * @param shipmentService the shipment service for querying and updating subsequent shipments
     * @param shipment the shipment to update
     * @param newValue the new value
     * @return the edit result containing all affected shipment IDs
     */
    public ShipmentEditResult updateValueWithRunningTotal(
            com.company.jmixspreadsheet.service.ShipmentService shipmentService,
            Shipment shipment,
            BigDecimal newValue) {
        if (shipmentService == null) {
            throw new IllegalArgumentException("ShipmentService cannot be null");
        }
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null");
        }
        if (shipment.getDay() == null || shipment.getPlant() == null || 
            shipment.getProduct() == null || shipment.getVessel() == null) {
            throw new IllegalArgumentException("Shipment day, plant, product, and vessel cannot be null");
        }

        boolean wasNew = entityStates.isNew(shipment);

        // Store old value for recalculation
        BigDecimal oldValue = shipment.getValue();
        if (oldValue == null) {
            oldValue = BigDecimal.ZERO;
        }

        // Update the shipment's value
        shipment.setValue(newValue);

        // Save using DataManager
        Shipment savedShipment = dataManager.save(shipment);

        // Get the entity ID
        UUID shipmentId = savedShipment.getId();

        // Find all subsequent shipments with same plant/product/vessel
        List<Shipment> subsequentShipments = shipmentService.findSubsequentShipments(
                savedShipment.getDay(),
                savedShipment.getPlant(),
                savedShipment.getProduct(),
                savedShipment.getVessel()
        );

        // Recalculate running totals for subsequent shipments
        Set<UUID> affectedIds = new HashSet<>();
        if (shipmentId != null) {
            affectedIds.add(shipmentId);
        }

        // Calculate current running total up to this shipment
        BigDecimal runningTotal = shipmentService.calculateRunningTotal(
                savedShipment.getDay(),
                savedShipment.getPlant(),
                savedShipment.getProduct(),
                savedShipment.getVessel()
        );
        // Add the new value to get the total including this shipment
        runningTotal = runningTotal.subtract(oldValue).add(newValue);

        // Update subsequent shipments using DataManager
        List<Shipment> shipmentsToSave = new ArrayList<>();
        for (Shipment subsequentShipment : subsequentShipments) {
            // Add the value difference to maintain running total
            BigDecimal currentValue = subsequentShipment.getValue();
            if (currentValue == null) {
                currentValue = BigDecimal.ZERO;
            }
            
            // Recalculate: new running total = previous running total + this shipment's value
            BigDecimal newSubsequentValue = runningTotal.add(currentValue);
            subsequentShipment.setValue(newSubsequentValue);
            
            // Update running total for next iteration
            runningTotal = newSubsequentValue;
            
            shipmentsToSave.add(subsequentShipment);
        }
        
        // Save all subsequent shipments using DataManager
        if (!shipmentsToSave.isEmpty()) {
            io.jmix.core.EntitySet savedEntitySet = dataManager.saveAll(shipmentsToSave);
            for (Object savedObj : savedEntitySet) {
                if (savedObj instanceof Shipment) {
                    Shipment saved = (Shipment) savedObj;
                    UUID subsequentId = saved.getId();
                    if (subsequentId != null) {
                        affectedIds.add(subsequentId);
                    }
                }
            }
        }

        return ShipmentEditResult.mixed(affectedIds, wasNew);
    }

    /**
     * Updates the value of a shipment via DataManager (simple update without running total).
     *
     * @param shipment the shipment to update
     * @param value the new value
     * @return the edit result containing affected shipment information
     */
    public ShipmentEditResult updateValue(Shipment shipment, BigDecimal value) {
        return updateProperty(shipment, "value", value);
    }

    /**
     * Updates the day of a shipment via DataManager.
     *
     * @param shipment the shipment to update
     * @param day the new day
     * @return the edit result containing affected shipment information
     */
    public ShipmentEditResult updateDay(Shipment shipment, LocalDate day) {
        return updateProperty(shipment, "day", day);
    }

    /**
     * Updates multiple shipments via DataManager.
     * <p>
     * This method updates multiple shipments and returns information about all affected shipments.
     * Useful for pivot table edits that affect multiple entities.
     *
     * @param propertyUpdates a map of shipments to their property updates
     * @return the edit result containing all affected shipment IDs
     */
    public ShipmentEditResult updateMultiple(
            Map<Shipment, Map<String, Object>> propertyUpdates) {
        if (propertyUpdates == null || propertyUpdates.isEmpty()) {
            throw new IllegalArgumentException("Property updates cannot be null or empty");
        }

        Set<UUID> affectedIds = new HashSet<>();
        boolean newEntityCreated = false;
        List<Shipment> shipmentsToSave = new ArrayList<>();

        // Update each shipment
        for (Map.Entry<Shipment, Map<String, Object>> entry : propertyUpdates.entrySet()) {
            Shipment shipment = entry.getKey();
            Map<String, Object> updates = entry.getValue();

            if (updates == null || updates.isEmpty()) {
                continue;
            }

            // Track if this is a new entity
            if (entityStates.isNew(shipment)) {
                newEntityCreated = true;
            }

            // Update all properties
            for (Map.Entry<String, Object> propertyEntry : updates.entrySet()) {
                String propertyName = propertyEntry.getKey();
                Object propertyValue = propertyEntry.getValue();
                io.jmix.core.entity.EntityValues.setValue(shipment, propertyName, propertyValue);
            }

            shipmentsToSave.add(shipment);
        }

        // Save all shipments using DataManager
        io.jmix.core.EntitySet savedEntitySet = dataManager.saveAll(shipmentsToSave);
        for (Object savedObj : savedEntitySet) {
            if (savedObj instanceof Shipment) {
                Shipment saved = (Shipment) savedObj;
                UUID shipmentId = saved.getId();
                if (shipmentId != null) {
                    affectedIds.add(shipmentId);
                }
            }
        }

        return ShipmentEditResult.mixed(affectedIds, newEntityCreated);
    }

    /**
     * Creates a new shipment via DataManager with running total calculation.
     * <p>
     * This method:
     * 1. Creates a new shipment with the specified value
     * 2. Calculates running total including this shipment
     * 3. Updates subsequent shipments with recalculated running totals
     * 4. Returns all affected entities
     *
     * @param shipmentService the shipment service for querying and updating subsequent shipments
     * @param day the day for the new shipment
     * @param plant the plant for the new shipment
     * @param product the product for the new shipment
     * @param vessel the vessel for the new shipment
     * @param value the value for the new shipment
     * @return the edit result containing all affected shipment IDs
     */
    public ShipmentEditResult createWithRunningTotal(
            com.company.jmixspreadsheet.service.ShipmentService shipmentService,
            LocalDate day,
            com.company.jmixspreadsheet.entity.Plant plant,
            com.company.jmixspreadsheet.entity.Product product,
            com.company.jmixspreadsheet.entity.Vessel vessel,
            BigDecimal value) {
        if (shipmentService == null) {
            throw new IllegalArgumentException("ShipmentService cannot be null");
        }
        if (day == null || plant == null || product == null || vessel == null) {
            throw new IllegalArgumentException("Day, plant, product, and vessel cannot be null");
        }

        // Calculate running total up to this day (before adding new shipment)
        BigDecimal runningTotal = shipmentService.calculateRunningTotal(day, plant, product, vessel);
        
        // Add the new shipment's value to the running total
        BigDecimal newValue = value != null ? value : BigDecimal.ZERO;
        runningTotal = runningTotal.add(newValue);

        // Create new shipment with running total
        Shipment shipment = dataManager.create(Shipment.class);
        shipment.setDay(day);
        shipment.setPlant(plant);
        shipment.setProduct(product);
        shipment.setVessel(vessel);
        shipment.setValue(runningTotal);

        // Save using DataManager
        Shipment savedShipment = dataManager.save(shipment);

        // Get the entity ID
        UUID shipmentId = savedShipment.getId();

        Set<UUID> affectedIds = new HashSet<>();
        if (shipmentId != null) {
            affectedIds.add(shipmentId);
        }

        // Find all subsequent shipments with same plant/product/vessel
        List<Shipment> subsequentShipments = shipmentService.findSubsequentShipments(
                day, plant, product, vessel);

        // Recalculate running totals for subsequent shipments
        List<Shipment> shipmentsToSave = new ArrayList<>();
        for (Shipment subsequentShipment : subsequentShipments) {
            // Add this shipment's value to maintain running total
            BigDecimal currentValue = subsequentShipment.getValue();
            if (currentValue == null) {
                currentValue = BigDecimal.ZERO;
            }
            
            BigDecimal newSubsequentValue = runningTotal.add(currentValue);
            subsequentShipment.setValue(newSubsequentValue);
            
            // Update running total for next iteration
            runningTotal = newSubsequentValue;
            
            shipmentsToSave.add(subsequentShipment);
        }
        
        // Save all subsequent shipments using DataManager
        if (!shipmentsToSave.isEmpty()) {
            io.jmix.core.EntitySet savedEntitySet = dataManager.saveAll(shipmentsToSave);
            for (Object savedObj : savedEntitySet) {
                if (savedObj instanceof Shipment) {
                    Shipment saved = (Shipment) savedObj;
                    UUID subsequentId = saved.getId();
                    if (subsequentId != null) {
                        affectedIds.add(subsequentId);
                    }
                }
            }
        }

        return ShipmentEditResult.mixed(affectedIds, true);
    }

    /**
     * Creates a new shipment via DataManager (simple creation without running total).
     *
     * @param day the day for the new shipment
     * @param plant the plant for the new shipment
     * @param product the product for the new shipment
     * @param vessel the vessel for the new shipment
     * @param value the value for the new shipment
     * @return the edit result containing the created shipment ID
     */
    public ShipmentEditResult create(
                                     LocalDate day,
                                     com.company.jmixspreadsheet.entity.Plant plant,
                                     com.company.jmixspreadsheet.entity.Product product,
                                     com.company.jmixspreadsheet.entity.Vessel vessel,
                                     BigDecimal value) {

        // Create new shipment using DataManager
        Shipment shipment = dataManager.create(Shipment.class);
        shipment.setDay(day);
        shipment.setPlant(plant);
        shipment.setProduct(product);
        shipment.setVessel(vessel);
        shipment.setValue(value);

        // Save using DataManager
        Shipment savedShipment = dataManager.save(shipment);

        // Get the entity ID
        UUID shipmentId = savedShipment.getId();

        if (shipmentId != null) {
            return ShipmentEditResult.created(shipmentId);
        } else {
            // If ID is not yet assigned, return empty result but mark as created
            return ShipmentEditResult.mixed(Collections.emptySet(), true);
        }
    }
}
