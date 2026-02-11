package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum LaycanStatus implements EnumClass<Integer> {
    
    ;
    
    private final Integer id;
    
    LaycanStatus(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Nullable
    public static LaycanStatus fromId(Integer id) {
        for (LaycanStatus at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}