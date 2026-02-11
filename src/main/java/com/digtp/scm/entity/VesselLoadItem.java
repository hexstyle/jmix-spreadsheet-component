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
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed", "JmixInstanceName"})
@JmixEntity
@Table(name = "SCM_VESSEL_LOAD_ITEM", indexes = {
        @Index(name = "IDX_SCM_VESSEL_LOAD_ITEM_VESSEL_LOAD", columnList = "VESSEL_LOAD_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_ITEM_PRE_PARTY", columnList = "PRE_PARTY_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_ITEM_PORT", columnList = "PORT_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_ITEM_INCOTERMS", columnList = "INCOTERMS_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_ITEM_TOLERANCE", columnList = "TOLERANCE_ID")
})
@Entity(name = "scm_VesselLoadItem")
public class VesselLoadItem {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "VESSEL_LOAD_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private VesselLoad vesselLoad;

    @JoinColumn(name = "PRE_PARTY_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PreParty preParty;

    @JoinColumn(name = "PORT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Port port;

    @JoinColumn(name = "INCOTERMS_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Incoterms incoterms;

    @NotNull
    @Column(name = "CLIENT", nullable = false, length = 200)
    private String client;

    @NotNull
    @PositiveOrZero(message = "{msg://com.digtp.scm.entity/VesselLoadItem.volume.validation.PositiveOrZero}")
    @Column(name = "VOLUME", nullable = false)
    private Integer volume;

    @JoinColumn(name = "TOLERANCE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Tolerance tolerance;

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

    public Tolerance getTolerance() {
        return tolerance;
    }

    public void setTolerance(Tolerance tolerance) {
        this.tolerance = tolerance;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Incoterms getIncoterms() {
        return incoterms;
    }

    public void setIncoterms(Incoterms incoterms) {
        this.incoterms = incoterms;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public PreParty getPreParty() {
        return preParty;
    }

    public void setPreParty(PreParty preParty) {
        this.preParty = preParty;
    }

    public VesselLoad getVesselLoad() {
        return vesselLoad;
    }

    public void setVesselLoad(VesselLoad vesselLoad) {
        this.vesselLoad = vesselLoad;
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