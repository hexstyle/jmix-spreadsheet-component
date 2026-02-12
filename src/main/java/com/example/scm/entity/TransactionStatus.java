package com.example.scm.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TransactionStatus implements EnumClass<Integer> {

    UNSOLD(10),
    SOLD(20);

    private final Integer id;

    TransactionStatus(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static TransactionStatus fromId(Integer id) {
        for (TransactionStatus at : TransactionStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}