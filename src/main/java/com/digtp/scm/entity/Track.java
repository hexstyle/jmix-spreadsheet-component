package com.digtp.scm.entity;

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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCM_TRACK", indexes = {
        @Index(name = "IDX_SCM_TRACK_PPIO_MONTH", columnList = "PPIO_MONTH_ID"),
        @Index(name = "IDX_SCM_TRACK_USER", columnList = "USER_ID"),
        @Index(name = "IDX_SCM_TRACK_PARENT_TRACK", columnList = "PARENT_TRACK_ID")
})
@Entity(name = "scm_Track")
public class Track {
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
    
    @InstanceName
    @Column(name = "NAME", nullable = false, length = 200)
    @NotNull
    private String name;
    
    @Column(name = "TRACK_TYPE", nullable = false)
    @NotNull
    private Integer trackType;
    
    @Column(name = "TRACK_STATUS")
    private Integer trackStatus;
    
    @JoinColumn(name = "PPIO_MONTH_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private PpioMonth ppioMonth;
    
    @Column(name = "FILLING_START_DATE")
    private LocalDate fillingStartDate;
    
    @Column(name = "FILLING_END_DATE")
    private LocalDate fillingEndDate;
    
    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    
    @Column(name = "IS_ACTIVE")
    private Boolean isActive = false;
    
    @Column(name = "IS_FROZEN")
    private Boolean isFrozen = false;
    
    @JoinColumn(name = "PARENT_TRACK_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Track parentTrack;
    
    public Track getParentTrack() {
        return parentTrack;
    }
    
    public void setParentTrack(Track parentTrack) {
        this.parentTrack = parentTrack;
    }
    
    public Boolean getIsFrozen() {
        return isFrozen;
    }
    
    public void setIsFrozen(Boolean isFrozen) {
        this.isFrozen = isFrozen;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getFillingEndDate() {
        return fillingEndDate;
    }
    
    public void setFillingEndDate(LocalDate fillingEndDate) {
        this.fillingEndDate = fillingEndDate;
    }
    
    public LocalDate getFillingStartDate() {
        return fillingStartDate;
    }
    
    public void setFillingStartDate(LocalDate fillingStartDate) {
        this.fillingStartDate = fillingStartDate;
    }
    
    public PpioMonth getPpioMonth() {
        return ppioMonth;
    }
    
    public void setPpioMonth(PpioMonth ppioMonth) {
        this.ppioMonth = ppioMonth;
    }
    
    public TrackStatus getTrackStatus() {
        return trackStatus == null ? null : TrackStatus.fromId(trackStatus);
    }
    
    public void setTrackStatus(TrackStatus trackStatus) {
        this.trackStatus = trackStatus == null ? null : trackStatus.getId();
    }
    
    public TrackType getTrackType() {
        return trackType == null ? null : TrackType.fromId(trackType);
    }
    
    public void setTrackType(TrackType trackType) {
        this.trackType = trackType == null ? null : trackType.getId();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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