package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum TechnologyScheme implements EnumClass<Integer> {
    
    WAREHOUSE_VESSEL(10),
    TRAIN_WAREHOUSE(20);
    
    private final Integer id;
    
    TechnologyScheme(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Nullable
    public static TechnologyScheme fromId(Integer id) {
        for (TechnologyScheme at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}