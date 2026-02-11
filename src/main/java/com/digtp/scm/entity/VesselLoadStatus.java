package com.digtp.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;

/**
 * Status of {@link VesselLoad}
 */
public enum VesselLoadStatus implements EnumClass<Integer> {

    DRAFT(10),
    READY_FOR_PLANNING(20),
    LAYCAN_REQUESTED(30),
    LAYCAN_CHANGES_REQUESTED(40),
    LAYCAN_REJECTED(50),
    LAYCAN_CONFIRMED(60),
    PLACING_ORDER(70),
    ORDER_PLACED(80),
    SHIPLOAD_CHANGES_REQUESTED(90),
    SHIPLOAD_CHANGES_REJECTED(100),
    CLEAN_FIXED(110);

    private final Integer id;

    VesselLoadStatus(Integer id) {
        this.id = id;
    }

    @Nonnull
    @Override
    public Integer getId() {
        return id;
    }

    @Nullable
    public static VesselLoadStatus fromId(Integer id) {
        for (VesselLoadStatus at : VesselLoadStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}