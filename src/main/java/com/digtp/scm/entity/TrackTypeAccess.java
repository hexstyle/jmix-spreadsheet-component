package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Access type for {@link TrackType}
 */
public enum TrackTypeAccess implements EnumClass<String> {
    
    VIEW("VIEW"),
    EDIT("EDIT");
    
    private final String id;
    
    TrackTypeAccess(String id) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Nullable
    public static TrackTypeAccess fromId(String id) {
        for (TrackTypeAccess at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}