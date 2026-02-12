package com.example.scm.entity;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCM_REASON", indexes = {
        @Index(name = "IDX_SCM_REASON_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_SCM_REASON_PRODUCT_PACKAGE", columnList = "PRODUCT_PACKAGE_ID"),
        @Index(name = "IDX_SCM_REASON_WAREHOUSE", columnList = "WAREHOUSE_ID"),
        @Index(name = "IDX_SCM_REASON_TRACK", columnList = "TRACK_ID"),
        @Index(name = "IDX_SCM_REASON_COUNTERPARTY_WAREHOUSE", columnList = "COUNTERPARTY_WAREHOUSE_ID"),
        @Index(name = "IDX_SCM_REASON_ORIGIN_PLANT", columnList = "ORIGIN_PLANT_ID"),
        @Index(name = "IDX_SCM_REASON_PARENT_REASON", columnList = "PARENT_REASON_ID"),
        @Index(name = "IDX_SCM_REASON_USER", columnList = "USER_ID"),
        @Index(name = "IDX_SCM_REASON_TRANSPORT_TYPE", columnList = "TRANSPORT_TYPE_ID"),
        @Index(name = "IDX_SCM_REASON_VESSEL_LOAD_ITEM", columnList = "VESSEL_LOAD_ITEM_ID")
})
@Entity(name = "scm_Reason")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
public abstract class Reason {
    
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
    
    @JoinColumn(name = "WAREHOUSE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WarehouseTerminal warehouse;
    
    @JoinColumn(name = "COUNTERPARTY_WAREHOUSE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse counterpartyWarehouse;
    
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
    
    @Positive
    @Column(name = "VOLUME", nullable = false)
    @NotNull
    private Integer volume;
    
    @JoinColumn(name = "TRACK_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Track track;
    
    @Column(name = "COMMENT_", length = 2000)
    private String comment;
    
    @Column(name = "IS_FACT")
    private Boolean isFact = false;
    
    @JoinColumn(name = "PARENT_REASON_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Reason parentReason;
    
    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    
    /**
     * Use this field in JPA queries with {@link #dtype} search
     *
     * @see PlantShipmentReason PlantShipmentReason - actual owner
     */
    @Column(name = "LEADTIME")
    protected Short leadtime;
    
    /**
     * Use this field in JPA queries with {@link #dtype} search
     *
     * @see TerminalWarehouseReceiptReason TerminalWarehouseReceiptReason - actual owner
     * @see PlantShipmentReason PlantShipmentReason - actual owner
     */
    @JoinColumn(name = "TRANSPORT_TYPE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    protected TransportType transportType;
    
    /**
     * Use this field in JPA queries with {@link #dtype} search
     *
     * @see VesselLoadingReason VesselLoadingReason - actual owner
     */
    @JoinColumn(name = "VESSEL_LOAD_ITEM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    protected VesselLoadItem vesselLoadItem;
    
    /**
     * Use this field only in JPA queries
     */
    @SystemLevel
    @Column(name = "DTYPE")
    @SuppressWarnings("JmixEntityColumnName")
    private String dtype;
    
    /**
     * @deprecated Do not call this method from {@link Reason}. Actual owner is {@link VesselLoadingReason}
     */
    @Deprecated
    public VesselLoadItem getVesselLoadItem() {
        return vesselLoadItem;
    }
    
    /**
     * @deprecated Do not call this method from {@link Reason}. Actual owner is {@link VesselLoadingReason}
     */
    @Deprecated
    public void setVesselLoadItem(VesselLoadItem vesselLoadItem) {
        this.vesselLoadItem = vesselLoadItem;
    }
    
    /**
     * @deprecated Do not call this method from {@link Reason}. Actual owner is {@link PlantShipmentReason}
     */
    @Deprecated
    public Short getLeadtime() {
        return leadtime;
    }
    
    /**
     * @deprecated Do not call this method from {@link Reason}. Actual owner is {@link PlantShipmentReason}
     */
    @Deprecated
    public void setLeadtime(Short leadtime) {
        this.leadtime = leadtime;
    }
    
    /**
     * @deprecated Do not call this method from {@link Reason}.
     * Actual owner is {@link PlantShipmentReason} and {@link TerminalWarehouseReceiptReason}
     */
    @Deprecated
    public TransportType getTransportType() {
        return transportType;
    }
    
    /**
     * @deprecated Do not call this method from {@link Reason}.
     * Actual owner is {@link PlantShipmentReason} and {@link TerminalWarehouseReceiptReason}
     */
    @Deprecated
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
    
    /**
     * @deprecated Do not manually use this method
     */
    @Deprecated
    public String getDtype() {
        return dtype;
    }
    
    /**
     * @deprecated Do not manually use this method
     */
    @Deprecated
    public void setDtype(String dtype) {
        this.dtype = dtype;
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
    
    public boolean isValid() {
        return true;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Reason getParentReason() {
        return parentReason;
    }
    
    public void setParentReason(Reason parentReason) {
        this.parentReason = parentReason;
    }
    
    public Boolean getIsFact() {
        return isFact;
    }
    
    public void setIsFact(Boolean isFact) {
        this.isFact = isFact;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
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
    
    public Warehouse getCounterpartyWarehouse() {
        return counterpartyWarehouse;
    }
    
    public void setCounterpartyWarehouse(Warehouse counterpartyWarehouse) {
        this.counterpartyWarehouse = counterpartyWarehouse;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getId() {
        return id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public ProductPackage getProductPackage() {
        return productPackage;
    }
    
    public void setProductPackage(ProductPackage productPackage) {
        this.productPackage = productPackage;
    }
    
    public WarehouseTerminal getWarehouse() {
        return warehouse;
    }
    
    public void setWarehouse(WarehouseTerminal warehouse) {
        this.warehouse = warehouse;
    }
    
    public Track getTrack() {
        return track;
    }
    
    public void setTrack(Track track) {
        this.track = track;
    }
}