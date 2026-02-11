package com.digtp.scm.entity;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
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
@Table(name = "SCM_STOCK", indexes = {
        @Index(name = "IDX_SCM_STOCK_WAREHOUSE", columnList = "WAREHOUSE_ID"),
        @Index(name = "IDX_SCM_STOCK_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_SCM_STOCK_PRODUCT_PACKAGE", columnList = "PRODUCT_PACKAGE_ID"),
        @Index(name = "IDX_SCM_STOCK_ORIGIN_PLANT", columnList = "ORIGIN_PLANT_ID"),
        @Index(name = "IDX_SCM_STOCK_TRACK", columnList = "TRACK_ID")
})
@Entity(name = "scm_Stock")
public class Stock {
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
    
    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;
    
    @DeletedDate
    @Column(name = "DELETED_DATE")
    private OffsetDateTime deletedDate;
    
    @JoinColumn(name = "WAREHOUSE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private WarehouseTerminal warehouse;
    
    @JoinColumn(name = "PRODUCT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    
    @JoinColumn(name = "PRODUCT_PACKAGE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductPackage productPackage;
    
    @JoinColumn(name = "ORIGIN_PLANT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Plant originPlant;
    
    @Column(name = "DATE_", nullable = false)
    @NotNull
    private LocalDate date;
    
    @Column(name = "VOLUME", nullable = false)
    @NotNull
    private Integer volume;
    
    @JoinColumn(name = "TRACK_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Track track;
    
    public OffsetDateTime getDeletedDate() {
        return deletedDate;
    }
    
    public void setDeletedDate(OffsetDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }
    
    public String getDeletedBy() {
        return deletedBy;
    }
    
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
    
    public Track getTrack() {
        return track;
    }
    
    public void setTrack(Track track) {
        this.track = track;
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
    
    public Plant getOriginPlant() {
        return originPlant;
    }
    
    public void setOriginPlant(Plant originPlant) {
        this.originPlant = originPlant;
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
    
    public WarehouseTerminal getWarehouse() {
        return warehouse;
    }
    
    public void setWarehouse(WarehouseTerminal warehouse) {
        this.warehouse = warehouse;
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