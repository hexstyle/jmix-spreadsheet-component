package com.digtp.scm.entity;

import com.digtp.scm.entity.trait.Archivable;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@JmixEntity
@Table(name = "SCM_PPIO_MONTH", indexes = {
        @Index(name = "IDX_SCM_PPIO_MONTH_UNQ_NAME", columnList = "NAME", unique = true),
        @Index(name = "IDX_SCM_PPIO_MONTH_UNQ_FIRST_DATE", columnList = "FIRST_DATE", unique = true)
})
@Entity(name = "scm_PpioMonth")
public class PpioMonth implements Archivable {
    
    @Min(197001)
    @Max(999912)
    @Column(name = "ID", nullable = false)
    @Id
    @NumberFormat(pattern = "######")
    private Integer id;
    
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
    
    @Column(name = "IS_ARCHIVED")
    private Boolean isArchived = false;
    
    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;
    
    @NotNull
    @Column(name = "FIRST_DATE", nullable = false)
    private LocalDate firstDate;
    
    public LocalDate getFirstDate() {
        return firstDate;
    }
    
    public void setFirstDate(LocalDate firstDate) {
        this.firstDate = firstDate;
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
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
}