package com.example.scm.entity;

import com.example.scm.entity.trait.Limitation;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCM_LEADTIME", indexes = {
        @Index(name = "IDX_SCM_LEADTIME_PLANT", columnList = "PLANT_ID"),
        @Index(name = "IDX_SCM_LEADTIME_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_SCM_LEADTIME_PRODUCT_PACKAGE", columnList = "PRODUCT_PACKAGE_ID"),
        @Index(name = "IDX_SCM_LEADTIME_TRANSPORT_TYPE", columnList = "TRANSPORT_TYPE_ID"),
        @Index(name = "IDX_SCM_LEADTIME_TERMINAL", columnList = "TERMINAL_ID")
})
@Entity(name = "scm_Leadtime")
public class Leadtime implements Limitation {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;
    
    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;
    
    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;
    
    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;
    
    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;
    
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;
    
    @JoinColumn(name = "PLANT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Plant plant;
    
    @JoinColumn(name = "PRODUCT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    
    @JoinColumn(name = "PRODUCT_PACKAGE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductPackage productPackage;
    
    @JoinColumn(name = "TERMINAL_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Terminal terminal;
    
    @JoinColumn(name = "TRANSPORT_TYPE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TransportType transportType;
    
    @Column(name = "CARGO_TYPE")
    private Integer cargoType;
    
    @Column(name = "DATE_START", nullable = false)
    @NotNull
    private LocalDate dateStart;
    
    @Column(name = "DATE_END", nullable = false)
    @NotNull
    private LocalDate dateEnd;
    
    @Column(name = "DAYS", nullable = false)
    @NotNull
    private Short days;
    
    public Short getDays() {
        return days;
    }
    
    public void setDays(Short days) {
        this.days = days;
    }
    
    public CargoType getCargoType() {
        return cargoType == null ? null : CargoType.fromId(cargoType);
    }
    
    public void setCargoType(CargoType cargoType) {
        this.cargoType = cargoType == null ? null : cargoType.getId();
    }
    
    public Terminal getTerminal() {
        return terminal;
    }
    
    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }
    
    @Override
    public LocalDate getDateEnd() {
        return dateEnd;
    }
    
    @Override
    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }
    
    @Override
    public LocalDate getDateStart() {
        return dateStart;
    }
    
    @Override
    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }
    
    public TransportType getTransportType() {
        return transportType;
    }
    
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
    
    public ProductPackage getProductPackage() {
        return productPackage;
    }
    
    public void setProductPackage(ProductPackage productPackage) {
        this.productPackage = productPackage;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Plant getPlant() {
        return plant;
    }
    
    public void setPlant(Plant plant) {
        this.plant = plant;
    }
    
    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
    
    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
}