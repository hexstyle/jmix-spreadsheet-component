package com.example.scm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@JmixEntity
@Entity(name = "scm_PlantShipmentReason")
@DiscriminatorValue(PlantShipmentReason.CODE)
public class PlantShipmentReason extends Reason {
    
    public static final String CODE = "PlantShipmentReason";
    
    public short getLeadtimeOrDefault() {
        return leadtime != null ? leadtime : 0;
    }
    
    @Override
    public boolean isValid() {
        return leadtime != null && (getParentReason() == null || getParentReason().isValid());
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public Short getLeadtime() {
        return leadtime;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setLeadtime(Short leadtime) {
        this.leadtime = leadtime;
    }
    
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
