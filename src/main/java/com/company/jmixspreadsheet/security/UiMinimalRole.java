package com.company.jmixspreadsheet.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.UiMinimalPolicies;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "UI: minimal access", code = UiMinimalRole.CODE, scope = SecurityScope.UI)
public interface UiMinimalRole extends UiMinimalPolicies {

    String CODE = "ui-minimal";

    @ViewPolicy(viewIds = "MainView")
    void main();

    @ViewPolicy(viewIds = "LoginView")
    @SpecificPolicy(resources = "ui.loginToUi")
    void login();
    
    @ViewPolicy(viewIds = "ShipmentSpreadsheet.list")
    @MenuPolicy(menuIds = "ShipmentSpreadsheet.list")
    void shipmentSpreadsheet();
}


