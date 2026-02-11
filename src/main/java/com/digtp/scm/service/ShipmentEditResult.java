package com.digtp.scm.service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Result of a shipment edit operation.
 * <p>
 * Contains information about which shipments were affected by the edit,
 * allowing the spreadsheet to update only the affected rows/cells.
 */
public class ShipmentEditResult {

    private final Set<UUID> affectedShipmentIds;
    private final boolean newEntityCreated;

    private ShipmentEditResult(Set<UUID> affectedShipmentIds, boolean newEntityCreated) {
        this.affectedShipmentIds = affectedShipmentIds != null 
                ? Collections.unmodifiableSet(affectedShipmentIds) 
                : Collections.emptySet();
        this.newEntityCreated = newEntityCreated;
    }

    /**
     * Creates a result for an edit that affected existing shipments.
     *
     * @param affectedShipmentIds the IDs of affected shipments
     * @return the edit result
     */
    public static ShipmentEditResult existing(Set<UUID> affectedShipmentIds) {
        return new ShipmentEditResult(affectedShipmentIds, false);
    }

    /**
     * Creates a result for an edit that created a new shipment.
     *
     * @param newShipmentId the ID of the newly created shipment
     * @return the edit result
     */
    public static ShipmentEditResult created(UUID newShipmentId) {
        return new ShipmentEditResult(Set.of(newShipmentId), true);
    }

    /**
     * Creates a result for an edit that affected both existing and new shipments.
     *
     * @param affectedShipmentIds the IDs of all affected shipments (including new ones)
     * @param newEntityCreated whether a new entity was created
     * @return the edit result
     */
    public static ShipmentEditResult mixed(Set<UUID> affectedShipmentIds, boolean newEntityCreated) {
        return new ShipmentEditResult(affectedShipmentIds, newEntityCreated);
    }

    /**
     * Returns the IDs of all affected shipments.
     * <p>
     * These IDs can be used to determine which rows/cells need updating in the spreadsheet.
     *
     * @return the set of affected shipment IDs
     */
    public Set<UUID> getAffectedShipmentIds() {
        return affectedShipmentIds;
    }

    /**
     * Returns whether a new entity was created by this edit.
     * <p>
     * If true, the spreadsheet may need to insert a new row rather than just updating existing cells.
     *
     * @return true if a new entity was created
     */
    public boolean isNewEntityCreated() {
        return newEntityCreated;
    }

    /**
     * Returns whether this edit affected any shipments.
     *
     * @return true if any shipments were affected
     */
    public boolean hasAffectedShipments() {
        return !affectedShipmentIds.isEmpty();
    }
}
