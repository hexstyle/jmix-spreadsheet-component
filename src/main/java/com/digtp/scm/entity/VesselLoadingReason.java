package com.digtp.scm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@JmixEntity
@Entity(name = "scm_VesselLoadingReason")
@DiscriminatorValue(VesselLoadingReason.CODE)
public class VesselLoadingReason extends Reason {
    
    public static final String CODE = "VesselLoadingReason";
    
    @Override
    @SuppressWarnings({"JpaAttributeTypeInspection", "deprecation"})
    public VesselLoadItem getVesselLoadItem() {
        return vesselLoadItem;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setVesselLoadItem(VesselLoadItem vesselLoadItem) {
        this.vesselLoadItem = vesselLoadItem;
    }
}