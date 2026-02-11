package com.digtp.scm.service;

import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.Shipment;
import com.digtp.scm.entity.Vessel;
import com.hexstyle.jmixspreadsheet.api.PivotEditStrategy;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Edit strategy for Shipment pivot cells.
 * <p>
 * This strategy handles edits to pivot cells in the Shipment spreadsheet.
 * When a pivot cell is edited (which represents an aggregated value),
 * this strategy determines which source entities should be modified.
 * <p>
 * Note: This strategy returns edits that will be applied by PivotTableCellEditor,
 * which saves entities using DataManager.
 * <p>
 * Currently handles the "value" measure which represents the SUM of shipment values.
 * When editing a pivot cell, if there are contributing entities, updates the value
 * property on the first entity. The cumulative sum field is calculated dynamically
 * from the collection and will automatically update when the layout is rebuilt.
 * If there are no contributing entities (empty pivot cell), creates a new shipment entity.
 */
@Component("jmixspreadsheet_ShipmentPivotEditStrategy")
public class ShipmentPivotEditStrategy implements PivotEditStrategy<Shipment> {

    private final ShipmentService shipmentService;
    private final DataManager dataManager;

    /**
     * Creates a new shipment pivot edit strategy.
     *
     * @param shipmentService the shipment service for finding subsequent shipments
     * @param dataManager the data manager for entity lookups
     */
    @Autowired
    public ShipmentPivotEditStrategy(ShipmentService shipmentService, DataManager dataManager) {
        if (shipmentService == null) {
            throw new IllegalArgumentException("ShipmentService cannot be null");
        }
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager cannot be null");
        }
        this.shipmentService = shipmentService;
        this.dataManager = dataManager;
    }

    /**
     * Determines which shipments should be modified when a pivot cell is edited.
     * <p>
     * For the "value" measure (SUM aggregation):
     * - If there are contributing entities, updates the first entity's value
     * - Includes subsequent shipments in the edits map (with empty property updates)
     *   so their cumulative sum cells get refreshed in the GUI
     * - The cumulative sum field is calculated dynamically from the collection
     * - If there are no contributing entities, creates a new shipment entity
     *
     * @param pivotContext the context identifying the pivot cell and its contributing entities
     * @param newValue the new value entered by the user
     * @return a map of shipments to their new property values (empty maps for refresh-only entities)
     */
    @Override
    public Map<Shipment, Map<String, Object>> determineEdits(PivotEditContext<Shipment> pivotContext, Object newValue) {
        // Use IdentityHashMap to avoid hashCode() issues with entities that don't have IDs yet
        // IdentityHashMap uses System.identityHashCode() instead of the entity's hashCode() method
        // This is safe for entities that haven't been saved yet (IDs are null)
        Map<Shipment, Map<String, Object>> edits = new java.util.IdentityHashMap<>();

        // Get contributing entities
        List<Shipment> contributingEntities = pivotContext.getContributingEntities();

        // Get measure ID
        String measureId = pivotContext.getMeasureId();

        // Handle "value" measure (SUM of shipment values)
        if ("value".equals(measureId)) {
            // Convert newValue to BigDecimal
            BigDecimal newBigDecimalValue = convertToBigDecimal(newValue);
            if (newBigDecimalValue == null) {
                // Cannot convert - return empty edits
                return edits;
            }

            // Extract row and column axis values once
            Map<String, Object> rowAxisValues = pivotContext.getRowAxisValues();
            Map<String, Object> columnAxisValues = pivotContext.getColumnAxisValues();
            
            // Debug: Log rowAxisValues to see what we have
            // This helps diagnose why day might be null
            if (rowAxisValues != null && !rowAxisValues.containsKey("day")) {
                // Day is not in rowAxisValues - this means extraction failed in PivotTableCellEditor
                // This can happen if the Day column binding doesn't exist or has no value
            }
            
            // Check if we have contributing entities (including newly created ones)
            // The editor should create entities and pass them here
            Shipment targetShipment = null;
            if (!contributingEntities.isEmpty()) {
                // Use the first contributing entity (may be a newly created one without ID yet)
                // This entity was created by the editor
                targetShipment = contributingEntities.get(0);
            }
            
            // If targetShipment is null, it means the editor didn't create an entity
            // This shouldn't happen for empty pivot cells, but we'll handle it gracefully
            if (targetShipment == null) {
                // No contributing entities - return empty edits
                // The editor should have created an entity first
                return edits;
            } else {
                // Entity already exists (was created by editor or already exists)
                // Ensure all required fields are set from axis values if not already set
                // Extract axis values
                java.time.LocalDate day = extractDay(rowAxisValues);
                Plant plant = extractPlant(columnAxisValues);
                Product product = extractProduct(columnAxisValues);
                Vessel vessel = extractVessel(columnAxisValues);
                
                // Set missing fields directly on the entity
                if (targetShipment.getDay() == null && day != null) {
                    targetShipment.setDay(day);
                }
                if (targetShipment.getPlant() == null && plant != null) {
                    targetShipment.setPlant(plant);
                }
                if (targetShipment.getProduct() == null && product != null) {
                    targetShipment.setProduct(product);
                }
                if (targetShipment.getVessel() == null && vessel != null) {
                    targetShipment.setVessel(vessel);
                }
            }
            
            // Set the value (cumulative sum will be recalculated automatically from the collection)
            // Add edit for the target entity (either existing or newly created)
            Map<String, Object> propertyUpdates = new HashMap<>();
            propertyUpdates.put("value", newBigDecimalValue);
            
            // Also add day to property updates if not already set (ensures it's applied even if entity was modified above)
            if (targetShipment.getDay() == null) {
                java.time.LocalDate day = extractDay(rowAxisValues);
                if (day != null) {
                    propertyUpdates.put("day", day);
                }
            }
            
            // Only add axis values to property updates if they're not already set (ensures they're applied)
            if (targetShipment.getPlant() == null) {
                Plant plant = extractPlant(columnAxisValues);
                if (plant != null) {
                    propertyUpdates.put("plant", plant);
                }
            }
            if (targetShipment.getProduct() == null) {
                Product product = extractProduct(columnAxisValues);
                if (product != null) {
                    propertyUpdates.put("product", product);
                }
            }
            if (targetShipment.getVessel() == null) {
                Vessel vessel = extractVessel(columnAxisValues);
                if (vessel != null) {
                    propertyUpdates.put("vessel", vessel);
                }
            }
            
            // Use the target shipment (may be a managed entity without ID yet)
            // The editor's applyEdits will merge it if needed
            edits.put(targetShipment, propertyUpdates);
            
            // Include subsequent shipments in edits (with empty property updates)
            // so their cumulative sum cells get refreshed in the GUI
            // They won't be modified (empty property updates), but will be included in affected entities
            java.time.LocalDate day = targetShipment.getDay();
            if (day == null && propertyUpdates.containsKey("day")) {
                Object dayObj = propertyUpdates.get("day");
                if (dayObj instanceof java.time.LocalDate) {
                    day = (java.time.LocalDate) dayObj;
                }
            }
            if (day == null) {
                day = extractDay(rowAxisValues);
            }
            
            Plant plant = targetShipment.getPlant();
            if (plant == null && propertyUpdates.containsKey("plant")) {
                Object plantObj = propertyUpdates.get("plant");
                if (plantObj instanceof Plant) {
                    plant = (Plant) plantObj;
                }
            }
            if (plant == null) {
                plant = extractPlant(columnAxisValues);
            }
            
            Product product = targetShipment.getProduct();
            if (product == null && propertyUpdates.containsKey("product")) {
                Object productObj = propertyUpdates.get("product");
                if (productObj instanceof Product) {
                    product = (Product) productObj;
                }
            }
            if (product == null) {
                product = extractProduct(columnAxisValues);
            }
            
            Vessel vessel = targetShipment.getVessel();
            if (vessel == null && propertyUpdates.containsKey("vessel")) {
                Object vesselObj = propertyUpdates.get("vessel");
                if (vesselObj instanceof Vessel) {
                    vessel = (Vessel) vesselObj;
                }
            }
            if (vessel == null) {
                vessel = extractVessel(columnAxisValues);
            }
            
            // Find subsequent shipments and add them to edits with empty property updates
            // This ensures their cells (including cumulative sum) are refreshed in the GUI
            if (day != null && plant != null && product != null && vessel != null) {
                List<Shipment> subsequentShipments = shipmentService.findSubsequentShipments(
                        day, plant, product, vessel);
                
                // Add each subsequent shipment with empty property updates
                // They will be included in affected entities for incremental update,
                // but won't be modified (empty property updates are skipped in applyEdits)
                for (Shipment subsequentShipment : subsequentShipments) {
                    // Use empty map to mark for refresh without modification
                    edits.put(subsequentShipment, new HashMap<>());
                }
            }
        }

        return edits;
    }

    /**
     * Converts an object to BigDecimal.
     *
     * @param value the value to convert
     * @return the BigDecimal value, or null if conversion fails
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * Extracts day (LocalDate) from row axis values.
     *
     * @param rowAxisValues the row axis values map
     * @return the day (LocalDate), or null if not found
     */
    private java.time.LocalDate extractDay(Map<String, Object> rowAxisValues) {
        if (rowAxisValues == null) {
            return null;
        }
        
        Object dayValue = rowAxisValues.get("day");
        if (dayValue == null) {
            return null;
        }
        
        if (dayValue instanceof java.time.LocalDate) {
            return (java.time.LocalDate) dayValue;
        }
        
        // Try to convert from other types
        if (dayValue instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) dayValue).toLocalDate();
        }
        
        if (dayValue instanceof java.util.Date) {
            java.util.Date dateValue = (java.util.Date) dayValue;
            return java.time.LocalDateTime.ofInstant(
                    dateValue.toInstant(), 
                    java.time.ZoneId.systemDefault()).toLocalDate();
        }
        
        // Try to parse from string - handle various formats
        if (dayValue instanceof String) {
            String valueStr = ((String) dayValue).trim();
            if (!valueStr.isEmpty()) {
                // Try ISO date format first (e.g., "2024-01-12")
                try {
                    return java.time.LocalDate.parse(valueStr);
                } catch (Exception e) {
                    // Try ISO datetime format (e.g., "2024-01-12T08:00:00")
                    try {
                        return java.time.LocalDateTime.parse(valueStr).toLocalDate();
                    } catch (Exception e2) {
                        // Try with custom format with seconds
                        try {
                            java.time.format.DateTimeFormatter formatter = 
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            return java.time.LocalDateTime.parse(valueStr, formatter).toLocalDate();
                        } catch (Exception e3) {
                            // Try with custom format without seconds
                            try {
                                java.time.format.DateTimeFormatter formatter = 
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                return java.time.LocalDateTime.parse(valueStr, formatter).toLocalDate();
                            } catch (Exception e4) {
                                // Try common date formats
                                try {
                                    java.time.format.DateTimeFormatter formatter = 
                                            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
                                    return java.time.LocalDate.parse(valueStr, formatter);
                                } catch (Exception e5) {
                                    try {
                                        java.time.format.DateTimeFormatter formatter = 
                                                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
                                        return java.time.LocalDate.parse(valueStr, formatter);
                                    } catch (Exception e6) {
                                        // Cannot parse - return null
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extracts Plant entity from column axis values.
     *
     * @param columnAxisValues the column axis values map
     * @return the Plant entity, or null if not found
     */
    private Plant extractPlant(Map<String, Object> columnAxisValues) {
        if (columnAxisValues == null) {
            return null;
        }
        
        Object plantValue = columnAxisValues.get("plant");
        if (plantValue == null) {
            return null;
        }
        
        if (plantValue instanceof Plant) {
            return (Plant) plantValue;
        }
        
        // Try to find by name
        String plantName = plantValue.toString();
        return findPlantByName(plantName);
    }

    /**
     * Finds a Plant entity by its name.
     *
     * @param name the name to find
     * @return the Plant entity, or null if not found
     */
    private Plant findPlantByName(String name) {
        try {
            return dataManager.load(Plant.class)
                    .query("select p from Plant p where p.name = :name")
                    .parameter("name", name)
                    .optional()
                    .orElse(null);
        } catch (Exception e) {
            // Ignore - cannot find plant
            return null;
        }
    }

    /**
     * Extracts Product entity from column axis values.
     *
     * @param columnAxisValues the column axis values map
     * @return the Product entity, or null if not found
     */
    private Product extractProduct(Map<String, Object> columnAxisValues) {
        if (columnAxisValues == null) {
            return null;
        }
        
        Object productValue = columnAxisValues.get("product");
        if (productValue == null) {
            return null;
        }
        
        if (productValue instanceof Product) {
            return (Product) productValue;
        }
        
        // Try to find by name
        String productName = productValue.toString();
        return findProductByName(productName);
    }

    /**
     * Finds a Product entity by its name.
     *
     * @param name the name to find
     * @return the Product entity, or null if not found
     */
    private Product findProductByName(String name) {
        try {
            return dataManager.load(Product.class)
                    .query("select p from Product p where p.name = :name")
                    .parameter("name", name)
                    .optional()
                    .orElse(null);
        } catch (Exception e) {
            // Ignore - cannot find product
            return null;
        }
    }

    /**
     * Extracts Vessel entity from column axis values.
     *
     * @param columnAxisValues the column axis values map
     * @return the Vessel entity, or null if not found
     */
    private Vessel extractVessel(Map<String, Object> columnAxisValues) {
        if (columnAxisValues == null) {
            return null;
        }
        
        Object vesselValue = columnAxisValues.get("vessel");
        if (vesselValue == null) {
            return null;
        }
        
        if (vesselValue instanceof Vessel) {
            return (Vessel) vesselValue;
        }
        
        // Try to find by name
        String vesselName = vesselValue.toString();
        return findVesselByName(vesselName);
    }

    /**
     * Finds a Vessel entity by its name.
     *
     * @param name the name to find
     * @return the Vessel entity, or null if not found
     */
    private Vessel findVesselByName(String name) {
        try {
            return dataManager.load(Vessel.class)
                    .query("select v from Vessel v where v.name = :name")
                    .parameter("name", name)
                    .optional()
                    .orElse(null);
        } catch (Exception e) {
            // Ignore - cannot find vessel
            return null;
        }
    }
}
