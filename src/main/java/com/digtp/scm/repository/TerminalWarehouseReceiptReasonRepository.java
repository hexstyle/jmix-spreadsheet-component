package com.digtp.scm.repository;

import com.digtp.scm.entity.Reason;
import com.digtp.scm.entity.TerminalWarehouseReceiptReason;
import io.jmix.core.repository.JmixDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerminalWarehouseReceiptReasonRepository extends JmixDataRepository<TerminalWarehouseReceiptReason, UUID> {
    
    Optional<TerminalWarehouseReceiptReason> findByParentReason(Reason parentReason);
}