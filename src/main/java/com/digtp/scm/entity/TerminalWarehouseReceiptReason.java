package com.digtp.scm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@JmixEntity
@Entity(name = "scm_TerminalWarehouseReceiptReason")
@DiscriminatorValue(TerminalWarehouseReceiptReason.CODE)
public class TerminalWarehouseReceiptReason extends Reason {
    
    public static final String CODE = "TerminalWarehouseReceiptReason";
    
    @Override
    @SuppressWarnings({"JpaAttributeTypeInspection", "deprecation"})
    public TransportType getTransportType() {
        return transportType;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
}