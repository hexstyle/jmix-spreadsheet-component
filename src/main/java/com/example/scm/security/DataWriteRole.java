package com.example.scm.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

/**
 * Data role for writing Shipment entities.
 * <p>
 * This role provides write access (CREATE, UPDATE, DELETE) to Shipment entities.
 */
@ResourceRole(name = "Data: Write Shipment", code = DataWriteRole.CODE)
public interface DataWriteRole {

    String CODE = "data-write-shipment";

    @EntityPolicy(entityName = "Shipment", actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE, EntityPolicyAction.DELETE})
    @EntityAttributePolicy(entityName = "Shipment", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void shipmentWrite();
    
    // FlowUI filter configuration permissions (required for genericFilter component)
    @EntityPolicy(entityName = "flowui_FilterConfiguration", actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ, EntityPolicyAction.UPDATE, EntityPolicyAction.DELETE})
    @EntityAttributePolicy(entityName = "flowui_FilterConfiguration", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @SpecificPolicy(resources = {"ui.genericfilter.modifyConfiguration", "ui.genericfilter.modifyGlobalConfiguration"})
    void filterConfiguration();
}
