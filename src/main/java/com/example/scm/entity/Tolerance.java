package com.example.scm.entity;

import com.example.scm.entity.trait.Archivable;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCM_TOLERANCE", indexes = {
        @Index(name = "IDX_SCM_TOLERANCE_UNQ", columnList = "NAME", unique = true)
})
@Entity(name = "scm_Tolerance")
public class Tolerance implements Archivable {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;
    
    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;
    
    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;
    
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;
    
    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;
    
    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;
    
    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;
    
    @DeletedDate
    @Column(name = "DELETED_DATE")
    private OffsetDateTime deletedDate;
    
    @Column(name = "IS_ARCHIVED")
    private Boolean isArchived = false;
    
    @InstanceName
    @Column(name = "NAME", nullable = false, length = 200)
    @NotNull
    private String name;
    
    @NotNull
    @Column(name = "MIN_", nullable = false, precision = 8, scale = 2)
    private BigDecimal min;
    
    @NotNull
    @Column(name = "MAX_", nullable = false, precision = 8, scale = 2)
    private BigDecimal max;
    
    public BigDecimal getMax() {
        return max;
    }
    
    public void setMax(BigDecimal max) {
        this.max = max;
    }
    
    public BigDecimal getMin() {
        return min;
    }
    
    public void setMin(BigDecimal min) {
        this.min = min;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public Boolean getIsArchived() {
        return isArchived;
    }
    
    @Override
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }
    
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