package com.digtp.scm.entity;

import com.digtp.scm.entity.trait.Archivable;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
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

@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
@JmixEntity
@Table(name = "SCM_PREPARTY", indexes = {
        @Index(name = "IDX_SCM_T_PREPARTY_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_SCM_PREPARTY_PRODUCT_PACKAGE", columnList = "PRODUCT_PACKAGE_ID"),
        @Index(name = "IDX_SCM_PREPARTY_PLANT", columnList = "ORIGIN_PLANT_ID"),
        @Index(name = "IDX_SCM_PREPARTY_TOLERANCE", columnList = "TOLERANCE_ID"),
        @Index(name = "IDX_SCM_PREPARTY_TERMINAL", columnList = "LOADING_TERMINAL_ID"),
        @Index(name = "IDX_SCM_PREPARTY_PORT", columnList = "UNLOADING_PORT_ID"),
        @Index(name = "IDX_SCM_PREPARTY_INCOTERMS", columnList = "INCOTERMS_ID"),
        @Index(name = "IDX_SCM_PREPARTY_PRODUCT_NORMAL", columnList = "PRODUCT_NORMAL_ID")
})
@Entity(name = "scm_PreParty")
public class PreParty implements Archivable {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "EXTERNAL_ID", length = 50)
    private String externalId;

    @NotNull
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @JoinColumn(name = "PRODUCT_NORMAL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductNormal productNormal;

    @JoinColumn(name = "PRODUCT_PACKAGE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductPackage productPackage;

    @NotNull
    @JoinColumn(name = "ORIGIN_PLANT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Plant originPlant;

    @NotNull
    @Column(name = "SHIPMENT_START_DATE", nullable = false)
    private LocalDate shipmentStartDate;

    @NotNull
    @Column(name = "SHIPMENT_END_DATE", nullable = false)
    private LocalDate shipmentEndDate;

    @NotNull
    @JoinColumn(name = "LOADING_TERMINAL_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Terminal loadingTerminal;

    @NotNull
    @JoinColumn(name = "UNLOADING_PORT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Port unloadingPort;

    @NotNull
    @JoinColumn(name = "INCOTERMS_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Incoterms incoterms;

    @NotNull
    @JoinColumn(name = "TOLERANCE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Tolerance tolerance;

    @Column(name = "SALES_CHANNEL", nullable = false)
    @NotNull
    private Integer salesChannel;

    @NotNull
    @Column(name = "TRANSACTION_STATUS", nullable = false)
    private Integer transactionStatus;

    @NotNull
    @Column(name = "VOLUME", nullable = false)
    private Integer volume;

    @Column(name = "CLIENT", length = 200)
    private String client;

    @InstanceName
    @Column(name = "COMMENT_")
    private String comment;

    @Column(name = "IS_ARCHIVED", nullable = false)
    @NotNull
    private Boolean isArchived = false;

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

    @PostConstruct
    protected void init() {
        setSalesChannel(SalesChannel.RSO);
        setTransactionStatus(TransactionStatus.UNSOLD);
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus == null ? null : transactionStatus.getId();
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus == null ? null : TransactionStatus.fromId(transactionStatus);
    }

    public SalesChannel getSalesChannel() {
        return salesChannel == null ? null : SalesChannel.fromId(salesChannel);
    }

    public void setSalesChannel(SalesChannel salesChannel) {
        this.salesChannel = salesChannel == null ? null : salesChannel.getId();
    }

    public ProductNormal getProductNormal() {
        return productNormal;
    }

    public void setProductNormal(ProductNormal productNormal) {
        this.productNormal = productNormal;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Incoterms getIncoterms() {
        return incoterms;
    }

    public void setIncoterms(Incoterms incoterms) {
        this.incoterms = incoterms;
    }

    public LocalDate getShipmentEndDate() {
        return shipmentEndDate;
    }

    public void setShipmentEndDate(LocalDate shipmentEndDate) {
        this.shipmentEndDate = shipmentEndDate;
    }

    public LocalDate getShipmentStartDate() {
        return shipmentStartDate;
    }

    public void setShipmentStartDate(LocalDate shipmentStartDate) {
        this.shipmentStartDate = shipmentStartDate;
    }

    public Port getUnloadingPort() {
        return unloadingPort;
    }

    public void setUnloadingPort(Port unloadingPort) {
        this.unloadingPort = unloadingPort;
    }

    public Terminal getLoadingTerminal() {
        return loadingTerminal;
    }

    public void setLoadingTerminal(Terminal loadingTerminal) {
        this.loadingTerminal = loadingTerminal;
    }

    public Tolerance getTolerance() {
        return tolerance;
    }

    public void setTolerance(Tolerance tolerance) {
        this.tolerance = tolerance;
    }

    public Plant getOriginPlant() {
        return originPlant;
    }

    public void setOriginPlant(Plant originPlant) {
        this.originPlant = originPlant;
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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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