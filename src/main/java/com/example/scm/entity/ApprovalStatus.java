package com.example.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ApprovalStatus implements EnumClass<Integer> {
    
    ;
    
    private final Integer id;
    
    ApprovalStatus(Integer id) {
        this.id = id;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Nullable
    public static ApprovalStatus fromId(Integer id) {
        for (ApprovalStatus at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}