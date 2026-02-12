package com.example.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Mode of {@link TransportCategory}
 */
public enum TransportMode implements EnumClass<Integer> {

    LAND(10),
    SEA(20);
    
    private final Integer id;
    
    TransportMode(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Nullable
    public static TransportMode fromId(Integer id) {
        for (TransportMode at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}