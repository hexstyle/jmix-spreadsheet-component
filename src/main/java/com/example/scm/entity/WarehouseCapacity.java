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
@Table(name = "SCM_WAREHOUSE_CAPACITY", indexes = {
        @Index(name = "IDX_SCM_WAREHOUSE_CAPACITY_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_SCM_WAREHOUSE_CAPACITY_PRODUCT_PACKAGE", columnList = "PRODUCT_PACKAGE_ID"),
        @Index(name = "IDX_SCM_WAREHOUSE_CAPACITY_WAREHOUSE", columnList = "WAREHOUSE_ID")
})
@Entity(name = "scm_WarehouseCapacity")
public class WarehouseCapacity implements Limitation {
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
    
    @JoinColumn(name = "PRODUCT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    
    @JoinColumn(name = "PRODUCT_PACKAGE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductPackage productPackage;
    
    @JoinColumn(name = "WAREHOUSE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Warehouse warehouse;
    
    @Column(name = "DATE_START", nullable = false)
    @NotNull
    private LocalDate dateStart;
    
    @Column(name = "DATE_END", nullable = false)
    @NotNull
    private LocalDate dateEnd;
    
    @Column(name = "CAPACITY", nullable = false)
    @NotNull
    private Integer capacity;
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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
    
    public Warehouse getWarehouse() {
        return warehouse;
    }
    
    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
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