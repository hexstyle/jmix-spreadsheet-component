package com.example.scm.entity;

import com.example.scm.entity.trait.Archivable;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
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
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCM_VESSEL", indexes = {
        @Index(name = "IDX_SCM_VESSEL_UNQ_IMO", columnList = "IMO", unique = true),
        @Index(name = "IDX_SCM_VESSEL_TRANSPORT_TYPE", columnList = "TRANSPORT_TYPE_ID")
})
@Entity(name = "scm_Vessel")
public class Vessel implements Archivable {
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
    
    @Column(name = "IS_ARCHIVED")
    private Boolean isArchived = false;
    
    @Pattern(regexp = "[0-9]{7}'", message = "{msg://constraints.Pattern.message.imo}")
    @Column(name = "IMO", nullable = false, length = 7)
    @NotNull
    private String imo;
    
    @InstanceName
    @Column(name = "NAME", nullable = false, length = 200)
    @NotNull
    private String name;
    
    @JoinColumn(name = "TRANSPORT_TYPE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TransportType transportType;
    
    @Column(name = "DEADWEIGHT")
    private Integer deadweight;
    
    public TransportType getTransportType() {
        return transportType;
    }
    
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
    
    public void setDeadweight(Integer deadweight) {
        this.deadweight = deadweight;
    }
    
    public Integer getDeadweight() {
        return deadweight;
    }
    
    public String getImo() {
        return imo;
    }
    
    public void setImo(String imo) {
        this.imo = imo;
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
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
}