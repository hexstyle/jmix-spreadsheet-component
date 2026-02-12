package com.example.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TrackType implements EnumClass<Integer> {

    BASIC(10),
    PLANNING(20),
    DRAFT(30),
    ARCHIVE(40);

    private final Integer id;

    TrackType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static TrackType fromId(Integer id) {
        for (TrackType at : TrackType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}