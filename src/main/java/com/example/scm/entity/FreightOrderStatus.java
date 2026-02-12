package com.example.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum FreightOrderStatus implements EnumClass<Integer> {
    
    ;
    
    private final Integer id;
    
    FreightOrderStatus(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Nullable
    public static FreightOrderStatus fromId(Integer id) {
        for (FreightOrderStatus at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}