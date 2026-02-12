package com.example.scm.repository;

import com.example.scm.entity.Reason;
import com.example.scm.entity.TerminalWarehouseReceiptReason;
import io.jmix.core.repository.JmixDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerminalWarehouseReceiptReasonRepository extends JmixDataRepository<TerminalWarehouseReceiptReason, UUID> {
    
    Optional<TerminalWarehouseReceiptReason> findByParentReason(Reason parentReason);
}