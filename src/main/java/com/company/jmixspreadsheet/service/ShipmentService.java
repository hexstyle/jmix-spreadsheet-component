package com.company.jmixspreadsheet.service;

import com.company.jmixspreadsheet.entity.Plant;
import com.company.jmixspreadsheet.entity.Product;
import com.company.jmixspreadsheet.entity.Shipment;
import com.company.jmixspreadsheet.entity.Vessel;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for CRUD operations on shipments.
 * <p>
 * This service provides basic CRUD operations and returns affected entities
 * for use with the spreadsheet's change analyzer.
 */
@Service
public class ShipmentService {

    @Autowired
    private DataManager dataManager;

    /**
     * Saves a shipment and returns all affected entities (including the saved one).
     *
     * @param shipment the shipment to save
     * @return the saved shipment
     */
    @Transactional
    public Shipment save(Shipment shipment) {
        return dataManager.save(shipment);
    }

    /**
     * Removes a shipment.
     *
     * @param shipment the shipment to remove
     */
    @Transactional
    public void remove(Shipment shipment) {
        dataManager.remove(shipment);
    }

    /**
     * Finds all shipments.
     *
     * @return list of all shipments
     */
    public List<Shipment> findAll() {
        return dataManager.load(Shipment.class)
                .all()
                .list();
    }

    /**
     * Finds a shipment by ID.
     *
     * @param id the shipment ID
     * @return the shipment, or null if not found
     */
    public Shipment findById(UUID id) {
        return dataManager.load(Shipment.class)
                .id(id)
                .one();
    }

    /**
     * Finds shipments with the same plant, product, and vessel,
     * where day is greater than the specified day, ordered by day ascending.
     *
     * @param day the day threshold
     * @param plant the plant
     * @param product the product
     * @param vessel the vessel
     * @return list of subsequent shipments with same plant/product/vessel
     */
    public List<Shipment> findSubsequentShipments(LocalDate day, Plant plant, 
                                                   Product product, Vessel vessel) {
        if (day == null || plant == null || product == null || vessel == null) {
            return Collections.emptyList();
        }

        return dataManager.load(Shipment.class)
                .query("select s from Shipment s " +
                       "where s.day > :day " +
                       "and s.plant = :plant " +
                       "and s.product = :product " +
                       "and s.vessel = :vessel " +
                       "order by s.day asc")
                .parameter("day", day)
                .parameter("plant", plant)
                .parameter("product", product)
                .parameter("vessel", vessel)
                .list();
    }

    /**
     * Finds shipments with the same plant, product, and vessel,
     * where day is less than or equal to the specified day, ordered by day ascending.
     * Used for calculating running totals.
     *
     * @param day the day threshold (inclusive)
     * @param plant the plant
     * @param product the product
     * @param vessel the vessel
     * @return list of shipments up to and including the specified day
     */
    public List<Shipment> findPreviousShipments(LocalDate day, Plant plant, 
                                                Product product, Vessel vessel) {
        if (day == null || plant == null || product == null || vessel == null) {
            return Collections.emptyList();
        }

        return dataManager.load(Shipment.class)
                .query("select s from Shipment s " +
                       "where s.day <= :day " +
                       "and s.plant = :plant " +
                       "and s.product = :product " +
                       "and s.vessel = :vessel " +
                       "order by s.day asc")
                .parameter("day", day)
                .parameter("plant", plant)
                .parameter("product", product)
                .parameter("vessel", vessel)
                .list();
    }

    /**
     * Calculates the running total for shipments up to and including the specified day.
     *
     * @param day the day threshold (inclusive)
     * @param plant the plant
     * @param product the product
     * @param vessel the vessel
     * @return the sum of values for shipments up to and including the specified day
     */
    public BigDecimal calculateRunningTotal(LocalDate day, Plant plant, 
                                           Product product, Vessel vessel) {
        List<Shipment> previousShipments = findPreviousShipments(day, plant, product, vessel);
        
        return previousShipments.stream()
                .map(Shipment::getValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the running total for shipments up to and including the specified day
     * from a collection of entities (instead of querying the database).
     * <p>
     * This method is used when calculating cumulative sums from entities in memory
     * (e.g., from a CollectionContainer), ensuring that changes are reflected immediately
     * even before they are saved to the database.
     *
     * @param allShipments all shipments in the collection
     * @param day the day threshold (inclusive)
     * @param plant the plant
     * @param product the product
     * @param vessel the vessel
     * @return the sum of values for shipments up to and including the specified day
     */
    public BigDecimal calculateRunningTotalFromCollection(
            Collection<Shipment> allShipments,
            LocalDate day,
            Plant plant,
            Product product,
            Vessel vessel) {
        if (day == null || plant == null || product == null || vessel == null) {
            return BigDecimal.ZERO;
        }

        return allShipments.stream()
                .filter(s -> s.getDay() != null && !s.getDay().isAfter(day))
                .filter(s -> Objects.equals(s.getPlant(), plant))
                .filter(s -> Objects.equals(s.getProduct(), product))
                .filter(s -> Objects.equals(s.getVessel(), vessel))
                .map(Shipment::getValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Updates the value of a shipment and recalculates running totals for subsequent shipments.
     * <p>
     * This method:
     * 1. Updates the shipment's value
     * 2. Finds all subsequent shipments (day > edited shipment's day) with same plant/product/vessel
     * 3. Recalculates running totals for each subsequent shipment
     * 4. Returns all affected entities (the edited one + all updated subsequent ones)
     *
     * @param shipment the shipment to update
     * @param newValue the new value for the shipment
     * @return list of all affected shipments (including the edited one and all subsequent ones)
     */
    @Transactional
    public List<Shipment> updateValueWithRunningTotal(Shipment shipment, BigDecimal newValue) {
        if (shipment == null || shipment.getDay() == null || 
            shipment.getPlant() == null || shipment.getProduct() == null || 
            shipment.getVessel() == null) {
            throw new IllegalArgumentException("Shipment and its required fields cannot be null");
        }

        // Store old value for recalculation
        BigDecimal oldValue = shipment.getValue();
        if (oldValue == null) {
            oldValue = BigDecimal.ZERO;
        }

        // Update the shipment's value
        shipment.setValue(newValue);
        Shipment savedShipment = dataManager.save(shipment);

        // Find all subsequent shipments with same plant/product/vessel
        List<Shipment> subsequentShipments = findSubsequentShipments(
                shipment.getDay(), 
                shipment.getPlant(), 
                shipment.getProduct(), 
                shipment.getVessel()
        );

        // Recalculate running totals for subsequent shipments
        List<Shipment> affectedShipments = new ArrayList<>();
        affectedShipments.add(savedShipment);

        BigDecimal runningTotal = calculateRunningTotal(
                shipment.getDay(), 
                shipment.getPlant(), 
                shipment.getProduct(), 
                shipment.getVessel()
        );

        for (Shipment subsequentShipment : subsequentShipments) {
            // Add the value difference to the running total
            runningTotal = runningTotal.add(subsequentShipment.getValue());
            subsequentShipment.setValue(runningTotal);
            Shipment saved = dataManager.save(subsequentShipment);
            affectedShipments.add(saved);
        }

        return affectedShipments;
    }

    /**
     * Creates a new shipment and recalculates running totals for subsequent shipments.
     *
     * @param shipment the shipment to create
     * @return list of all affected shipments (the new one + all subsequent ones that were updated)
     */
    @Transactional
    public List<Shipment> createWithRunningTotal(Shipment shipment) {
        if (shipment == null || shipment.getDay() == null || 
            shipment.getPlant() == null || shipment.getProduct() == null || 
            shipment.getVessel() == null) {
            throw new IllegalArgumentException("Shipment and its required fields cannot be null");
        }

        // Calculate running total up to this day (including this shipment's value)
        BigDecimal runningTotal = calculateRunningTotal(
                shipment.getDay(), 
                shipment.getPlant(), 
                shipment.getProduct(), 
                shipment.getVessel()
        );
        
        // Add the new shipment's value to the running total
        BigDecimal newValue = shipment.getValue() != null ? shipment.getValue() : BigDecimal.ZERO;
        runningTotal = runningTotal.add(newValue);

        // Set the running total as the shipment's value
        shipment.setValue(runningTotal);
        Shipment savedShipment = dataManager.save(shipment);

        // Find all subsequent shipments with same plant/product/vessel
        List<Shipment> subsequentShipments = findSubsequentShipments(
                shipment.getDay(), 
                shipment.getPlant(), 
                shipment.getProduct(), 
                shipment.getVessel()
        );

        // Recalculate running totals for subsequent shipments
        List<Shipment> affectedShipments = new ArrayList<>();
        affectedShipments.add(savedShipment);

        for (Shipment subsequentShipment : subsequentShipments) {
            runningTotal = runningTotal.add(subsequentShipment.getValue());
            subsequentShipment.setValue(runningTotal);
            Shipment saved = dataManager.save(subsequentShipment);
            affectedShipments.add(saved);
        }

        return affectedShipments;
    }
}
