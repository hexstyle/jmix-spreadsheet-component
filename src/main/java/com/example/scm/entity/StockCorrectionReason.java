package com.example.scm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@JmixEntity
@Entity(name = "scm_StockCorrectionReason")
@DiscriminatorValue(StockCorrectionReason.CODE)
public class StockCorrectionReason extends Reason {
    
    public static final String CODE = "StockCorrectionReason";
}