package com.company.jmixspreadsheet.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

/**
 * Data role for reading Shipment entities.
 * <p>
 * This role provides read-only access to Shipment entities for anonymous users.
 */
@ResourceRole(name = "Data: Read Shipment", code = DataReadRole.CODE)
public interface DataReadRole {

    String CODE = "data-read-shipment";

    @EntityPolicy(entityName = "Shipment", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "Shipment", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void shipmentRead();
}
