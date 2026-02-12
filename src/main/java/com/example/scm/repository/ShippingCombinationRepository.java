package com.example.scm.repository;

import com.example.scm.entity.ShippingCombination;
import io.jmix.core.repository.JmixDataRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ShippingCombinationRepository extends JmixDataRepository<ShippingCombination, UUID> {
}
