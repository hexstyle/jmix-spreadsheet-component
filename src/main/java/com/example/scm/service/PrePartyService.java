package com.example.scm.service;

import com.example.scm.entity.PreParty;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Helper for calculating some values of preliminary parties
 */
@Service
public  class PrePartyService {

    public static int calculateAvailableVolume(PreParty preParty, List<KeyValueEntity> vesselLoadVolumes) {
        var item = vesselLoadVolumes.stream()
                .filter(e -> e.getId().equals(preParty.getId()))
                .findFirst()
                .orElse(null);

        var volume = (item != null ? (Long) item.getValue("volume") : Long.valueOf(0));

        return preParty.getVolume() - volume.intValue();
    }

}
