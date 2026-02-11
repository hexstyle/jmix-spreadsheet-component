package com.digtp.scm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@JmixEntity
@Table(name = "SCM_WAREHOUSE_TERMINAL", indexes = {
        @Index(name = "IDX_SCM_WAREHOUSE_TERMINAL_TERMINAL", columnList = "TERMINAL_ID")
})
@Entity(name = "scm_WarehouseTerminal")
@PrimaryKeyJoinColumn(name = "ID")
public class WarehouseTerminal extends Warehouse {
    @JoinColumn(name = "TERMINAL_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Terminal terminal;
    
    public Terminal getTerminal() {
        return terminal;
    }
    
    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }
}