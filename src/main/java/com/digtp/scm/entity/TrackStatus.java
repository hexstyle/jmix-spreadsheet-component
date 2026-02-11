package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TrackStatus implements EnumClass<Integer> {

    DRAFT(10),
    COMPLETION(20),
    NEGOTIATION(30),
    APPROVED(40),
    DECLINED(50),
    ARCHIVE(60);

    private final Integer id;

    TrackStatus(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static TrackStatus fromId(Integer id) {
        for (TrackStatus at : TrackStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}