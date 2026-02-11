package com.digtp.scm.entity;

import com.digtp.scm.entity.trait.Archivable;
import com.digtp.scm.entity.trait.Limitation;
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
@Table(name = "SCM_TERMINAL_LOAD_CAPACITY", indexes = {
        @Index(name = "IDX_SCM_TERMINAL_LOAD_CAPACITY_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_SCM_TERMINAL_LOAD_CAPACITY_PRODUCT_PACKAGE", columnList = "PRODUCT_PACKAGE_ID"),
        @Index(name = "IDX_SCM_TERMINAL_LOAD_CAPACITY_TERMINAL", columnList = "TERMINAL_ID"),
        @Index(name = "IDX_SCM_TERMINAL_LOAD_CAPACITY_UNIQUE_ACTIVE", columnList = "PRODUCT_ID, PRODUCT_PACKAGE_ID, TECHNOLOGY_SCHEME, TERMINAL_ID, DATE_START", unique = true)
})
@Entity(name = "scm_TerminalLoadCapacity")
public class TerminalLoadCapacity implements Limitation, Archivable {
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

    @JoinColumn(name = "TERMINAL_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Terminal terminal;

    @Column(name = "DATE_START", nullable = false)
    @NotNull
    private LocalDate dateStart;

    @Column(name = "DATE_END")
    private LocalDate dateEnd;

    @Column(name = "TECHNOLOGY_SCHEME", nullable = false)
    @NotNull
    private Integer technologyScheme;

    @Column(name = "MAX_DAILY_VOLUME", nullable = false)
    @NotNull
    private Integer maxDailyVolume;

    @Column(name = "IS_ARCHIVED")
    private Boolean isArchived = false;

    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;

    @DeletedDate
    @Column(name = "DELETED_DATE")
    private OffsetDateTime deletedDate;

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

    public Integer getMaxDailyVolume() {
        return maxDailyVolume;
    }
    
    public void setMaxDailyVolume(Integer maxDailyVolume) {
        this.maxDailyVolume = maxDailyVolume;
    }
    
    public TechnologyScheme getTechnologyScheme() {
        return technologyScheme == null ? null : TechnologyScheme.fromId(technologyScheme);
    }
    
    public void setTechnologyScheme(TechnologyScheme technologyScheme) {
        this.technologyScheme = technologyScheme == null ? null : technologyScheme.getId();
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
    
    public Terminal getTerminal() {
        return terminal;
    }
    
    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
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

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }
    
}