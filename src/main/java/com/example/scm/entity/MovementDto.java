package com.example.scm.entity;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity(name = "scm_MovementDto")
public class MovementDto {
    
    @JmixId
    @InstanceName
    private UUID id;
    
    @JmixProperty(mandatory = true)
    @NotNull
    private LocalDate date;
    
    @JmixProperty(mandatory = true)
    @NotNull
    private Integer volume;
    
    @JmixProperty(mandatory = true)
    @NotNull
    private Short leadtime;
    
    public Short getLeadtime() {
        return leadtime;
    }
    
    public void setLeadtime(Short leadtime) {
        this.leadtime = leadtime;
    }
    
    public Integer getVolume() {
        return volume;
    }
    
    public void setVolume(Integer volume) {
        this.volume = volume;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
}