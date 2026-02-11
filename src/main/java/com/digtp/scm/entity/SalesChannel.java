package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum SalesChannel implements EnumClass<Integer> {

    RSO(10),
    B2B(20);

    private final Integer id;

    SalesChannel(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static SalesChannel fromId(Integer id) {
        for (SalesChannel at : SalesChannel.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}