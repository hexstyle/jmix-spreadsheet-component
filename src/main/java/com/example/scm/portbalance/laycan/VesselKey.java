package com.example.scm.portbalance.laycan;

import com.example.scm.entity.Vessel;
import com.example.scm.entity.VesselLoad;

import java.util.Objects;
import java.util.UUID;

public final class VesselKey {

    private final UUID vesselId;
    private final String vesselName;
    private final String normalizedName;
    private final Object fallbackKey;

    private VesselKey(UUID vesselId, String vesselName, Object fallbackKey) {
        this.vesselId = vesselId;
        this.vesselName = vesselName;
        this.normalizedName = normalize(vesselName);
        this.fallbackKey = fallbackKey;
    }

    public static VesselKey from(VesselLoad vesselLoad, String resolvedName) {
        if (vesselLoad == null) {
            throw new IllegalArgumentException("Vessel load is required");
        }
        Vessel vessel = vesselLoad.getVessel();
        UUID vesselId = vessel != null ? vessel.getId() : null;
        String vesselName = resolvedName != null ? resolvedName : vesselLoad.getVesselName();
        return new VesselKey(vesselId, vesselName, vesselLoad);
    }

    public UUID getVesselId() {
        return vesselId;
    }

    public String getVesselName() {
        return vesselName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VesselKey that)) {
            return false;
        }
        return Objects.equals(identityKey(), that.identityKey());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identityKey());
    }

    private Object identityKey() {
        if (vesselId != null) {
            return vesselId;
        }
        if (normalizedName != null) {
            return normalizedName;
        }
        return fallbackKey;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
