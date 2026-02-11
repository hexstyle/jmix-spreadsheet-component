package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum CargoType implements EnumClass<Integer> {

    SAFE(10),
    HAZARDOUS(20);
    
    private final Integer id;
    
    CargoType(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Nullable
    public static CargoType fromId(Integer id) {
        for (CargoType at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}