package com.example.scm.entity;

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

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCM_USER_TRACK_TYPE_ACCESS", indexes = {
        @Index(name = "IDX_SCM_USER_TRACK_TYPE_ACCESS_USER", columnList = "USER_ID"),
        @Index(name = "IDX_SCM_USER_TRACK_TYPE_ACCESS_UNQ", columnList = "USER_ID, TRACK_TYPE", unique = true)
})
@Entity(name = "scm_UserTrackTypeAccess")
public class UserTrackTypeAccess {
    
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;
    
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
    
    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;
    
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;
    
    @Column(name = "TRACK_TYPE", nullable = false)
    @NotNull
    private Integer trackType;
    
    @Column(name = "ACCESS_", nullable = false)
    @NotNull
    private String access;
    
    public TrackTypeAccess getAccess() {
        return access == null ? null : TrackTypeAccess.fromId(access);
    }
    
    public void setAccess(TrackTypeAccess access) {
        this.access = access == null ? null : access.getId();
    }
    
    public TrackType getTrackType() {
        return trackType == null ? null : TrackType.fromId(trackType);
    }
    
    public void setTrackType(TrackType trackType) {
        this.trackType = trackType == null ? null : trackType.getId();
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
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
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
}