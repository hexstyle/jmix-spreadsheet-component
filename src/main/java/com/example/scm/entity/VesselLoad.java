package com.example.scm.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
@JmixEntity
@Table(name = "SCM_VESSEL_LOAD", indexes = {
        @Index(name = "IDX_SCM_VESSEL_LOAD_TRACK", columnList = "TRACK_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_TERMINAL", columnList = "TERMINAL_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_TRANSPORT_TYPE", columnList = "TRANSPORT_TYPE_ID"),
        @Index(name = "IDX_SCM_VESSEL_LOAD_VESSEL", columnList = "VESSEL_ID")
})
@Entity(name = "scm_VesselLoad")
public class VesselLoad {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "TRACK_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Track track;

    @NotNull
    @Column(name = "TBN_NUM", nullable = false, length = 20)
    private String tbnNum;

    @JoinColumn(name = "TERMINAL_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Terminal terminal;

    @JoinColumn(name = "TRANSPORT_TYPE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TransportType transportType;

    @InstanceName
    @Column(name = "VESSEL_NAME", nullable = false, length = 200)
    @NotNull
    private String vesselName;

    @JoinColumn(name = "VESSEL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Vessel vessel;

    @Column(name = "PLANNING_LOADING_START_DATE", nullable = false)
    @NotNull
    private LocalDate planningLoadingStartDate;

    @Column(name = "PLANNING_LOADING_END_DATE", nullable = false)
    @NotNull
    private LocalDate planningLoadingEndDate;

    @Column(name = "ACTUAL_LOADING_START_DATE")
    private LocalDate actualLoadingStartDate;

    @Column(name = "ACTUAL_LOADING_END_DATE")
    private LocalDate actualLoadingEndDate;

    @Column(name = "PLANNING_LAYCAN_START_DATE", nullable = false)
    @NotNull
    private LocalDate planningLaycanStartDate;

    @Column(name = "PLANNING_LAYCAN_END_DATE", nullable = false)
    @NotNull
    private LocalDate planningLaycanEndDate;

    @Column(name = "ACTUAL_LAYCAN_START_DATE")
    private LocalDate actualLaycanStartDate;

    @Column(name = "ACTUAL_LAYCAN_END_DATE")
    private LocalDate actualLaycanEndDate;

    @Column(name = "VESSEL_LOAD_STATUS", nullable = false)
    @NotNull
    private Integer vesselLoadStatus;

    @Column(name = "BILL_OF_LOADING_DATE")
    private LocalDate billOfLoadingDate;

    @Column(name = "COMMENT_", length = 2000)
    private String comment;

    @Column(name = "TOTAL_VOLUME")
    private Integer totalVolume;

    @Column(name = "FREIGHT_ORDER", length = 20)
    private String freightOrder;

    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    @OneToMany(mappedBy = "vesselLoad")
    private List<VesselLoadItem> items;

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

    public String getFreightOrder() {
        return freightOrder;
    }

    public void setFreightOrder(String freightOrder) {
        this.freightOrder = freightOrder;
    }

    public String getTbnNum() {
        return tbnNum;
    }

    public void setTbnNum(String tbnNum) {
        this.tbnNum = tbnNum;
    }

    public List<VesselLoadItem> getItems() {
        return items;
    }

    public void setItems(List<VesselLoadItem> items) {
        this.items = items;
    }

    public Integer getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Integer totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getBillOfLoadingDate() {
        return billOfLoadingDate;
    }

    public void setBillOfLoadingDate(LocalDate billOfLoadingDate) {
        this.billOfLoadingDate = billOfLoadingDate;
    }

    @PostConstruct
    protected void init(){
        setVesselLoadStatus(VesselLoadStatus.DRAFT);
    }

    public VesselLoadStatus getVesselLoadStatus() {
        return vesselLoadStatus == null ? null : VesselLoadStatus.fromId(vesselLoadStatus);
    }

    public void setVesselLoadStatus(VesselLoadStatus vesselLoadStatus) {
        this.vesselLoadStatus = vesselLoadStatus == null ? null : vesselLoadStatus.getId();
    }

    public LocalDate getActualLaycanEndDate() {
        return actualLaycanEndDate;
    }

    public void setActualLaycanEndDate(LocalDate actualLaycanEndDate) {
        this.actualLaycanEndDate = actualLaycanEndDate;
    }

    public LocalDate getActualLaycanStartDate() {
        return actualLaycanStartDate;
    }

    public void setActualLaycanStartDate(LocalDate actualLaycanStartDate) {
        this.actualLaycanStartDate = actualLaycanStartDate;
    }

    public LocalDate getPlanningLaycanEndDate() {
        return planningLaycanEndDate;
    }

    public void setPlanningLaycanEndDate(LocalDate planningLaycanEndDate) {
        this.planningLaycanEndDate = planningLaycanEndDate;
    }

    public LocalDate getPlanningLaycanStartDate() {
        return planningLaycanStartDate;
    }

    public void setPlanningLaycanStartDate(LocalDate planningLaycanStartDate) {
        this.planningLaycanStartDate = planningLaycanStartDate;
    }

    public LocalDate getActualLoadingEndDate() {
        return actualLoadingEndDate;
    }

    public void setActualLoadingEndDate(LocalDate actualLoadingEndDate) {
        this.actualLoadingEndDate = actualLoadingEndDate;
    }

    public LocalDate getActualLoadingStartDate() {
        return actualLoadingStartDate;
    }

    public void setActualLoadingStartDate(LocalDate actualLoadingStartDate) {
        this.actualLoadingStartDate = actualLoadingStartDate;
    }

    public LocalDate getPlanningLoadingEndDate() {
        return planningLoadingEndDate;
    }

    public void setPlanningLoadingEndDate(LocalDate planningLoadingEndDate) {
        this.planningLoadingEndDate = planningLoadingEndDate;
    }

    public LocalDate getPlanningLoadingStartDate() {
        return planningLoadingStartDate;
    }

    public void setPlanningLoadingStartDate(LocalDate planningLoadingStartDate) {
        this.planningLoadingStartDate = planningLoadingStartDate;
    }

    public Vessel getVessel() {
        return vessel;
    }

    public void setVessel(Vessel vessel) {
        this.vessel = vessel;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
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