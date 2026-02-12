package com.example.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;


public enum Month implements EnumClass<Integer> {

    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12);

    private final Integer id;

    Month(Integer id) {
        this.id = id;
    }

    @Nonnull
    @Override
    public Integer getId() {
        return id;
    }

    @Nullable
    public static Month fromId(Integer id) {
        for (Month at : Month.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}