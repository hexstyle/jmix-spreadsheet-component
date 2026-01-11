package com.company.jmixspreadsheet.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SHIPMENT", indexes = {
        @Index(name = "IDX_SHIPMENT_ON_DAY", columnList = "DAY_")
})
@Entity
public class Shipment {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "DAY_", nullable = false)
    @NotNull
    private LocalDateTime day;

    @Column(name = "PLANT", nullable = false, length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Plant plant;

    @Column(name = "PRODUCT_", nullable = false, length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Product product;

    @Column(name = "VESSEL", nullable = false, length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Vessel vessel;

    @Column(name = "VALUE_", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal value;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getDay() {
        return day;
    }

    public void setDay(LocalDateTime day) {
        this.day = day;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Vessel getVessel() {
        return vessel;
    }

    public void setVessel(Vessel vessel) {
        this.vessel = vessel;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @InstanceName
    public String getInstanceName() {
        return String.format("%s - %s - %s", 
                day != null ? day.toString() : "", 
                plant != null ? plant.getCaption() : "", 
                product != null ? product.getCaption() : "");
    }
}
