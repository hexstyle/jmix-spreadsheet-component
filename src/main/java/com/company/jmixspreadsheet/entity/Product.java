package com.company.jmixspreadsheet.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Product implements EnumClass<String> {
    PRODUCT_1("PRODUCT_1"),
    PRODUCT_2("PRODUCT_2"),
    PRODUCT_3("PRODUCT_3"),
    PRODUCT_4("PRODUCT_4"),
    PRODUCT_5("PRODUCT_5"),
    PRODUCT_6("PRODUCT_6"),
    PRODUCT_7("PRODUCT_7"),
    PRODUCT_8("PRODUCT_8"),
    PRODUCT_9("PRODUCT_9"),
    PRODUCT_10("PRODUCT_10");

    private final String id;

    Product(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static Product fromId(String id) {
        for (Product product : Product.values()) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    public String getCaption() {
        return switch (this) {
            case PRODUCT_1 -> "Product 1";
            case PRODUCT_2 -> "Product 2";
            case PRODUCT_3 -> "Product 3";
            case PRODUCT_4 -> "Product 4";
            case PRODUCT_5 -> "Product 5";
            case PRODUCT_6 -> "Product 6";
            case PRODUCT_7 -> "Product 7";
            case PRODUCT_8 -> "Product 8";
            case PRODUCT_9 -> "Product 9";
            case PRODUCT_10 -> "Product 10";
        };
    }
}
