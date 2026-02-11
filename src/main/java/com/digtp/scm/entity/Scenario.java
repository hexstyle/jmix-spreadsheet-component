package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Scenario implements EnumClass<String> {
    
    PLAN("PLAN"),
    ACTUAL("ACTUAL"),
    FORECAST("FORECAST");
    
    private final String id;
    
    Scenario(String id) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Nullable
    public static Scenario fromId(String id) {
        for (Scenario at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}