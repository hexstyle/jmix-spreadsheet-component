package com.digtp.scm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@JmixEntity
@Entity(name = "scm_StockMovementReason")
@DiscriminatorValue(StockMovementReason.CODE)
public class StockMovementReason extends Reason {
    
    public static final String CODE = "StockMovementReason";
}