package com.company.jmixspreadsheet.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Plant implements EnumClass<String> {
    PLANT_A("PLANT_A"),
    PLANT_B("PLANT_B"),
    PLANT_C("PLANT_C"),
    PLANT_D("PLANT_D"),
    PLANT_E("PLANT_E");

    private final String id;

    Plant(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static Plant fromId(String id) {
        for (Plant plant : Plant.values()) {
            if (plant.getId().equals(id)) {
                return plant;
            }
        }
        return null;
    }

    public String getCaption() {
        return switch (this) {
            case PLANT_A -> "Plant A";
            case PLANT_B -> "Plant B";
            case PLANT_C -> "Plant C";
            case PLANT_D -> "Plant D";
            case PLANT_E -> "Plant E";
        };
    }
}
