package com.company.jmixspreadsheet.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Vessel implements EnumClass<String> {
    VESSEL_1("VESSEL_1"),
    VESSEL_2("VESSEL_2"),
    VESSEL_3("VESSEL_3"),
    VESSEL_4("VESSEL_4"),
    VESSEL_5("VESSEL_5");

    private final String id;

    Vessel(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static Vessel fromId(String id) {
        for (Vessel vessel : Vessel.values()) {
            if (vessel.getId().equals(id)) {
                return vessel;
            }
        }
        return null;
    }

    public String getCaption() {
        return switch (this) {
            case VESSEL_1 -> "Vessel 1";
            case VESSEL_2 -> "Vessel 2";
            case VESSEL_3 -> "Vessel 3";
            case VESSEL_4 -> "Vessel 4";
            case VESSEL_5 -> "Vessel 5";
        };
    }
}
