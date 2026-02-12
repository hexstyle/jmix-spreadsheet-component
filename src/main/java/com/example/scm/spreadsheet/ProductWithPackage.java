package com.example.scm.spreadsheet;

import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.ShippingCombination;
import lombok.Data;

@Data
public class ProductWithPackage {
    
    private final Product product;
    private final ProductPackage productPackage;
    
    public ProductWithPackage(ShippingCombination combination) {
        this.product = combination.getProduct();
        this.productPackage = combination.getProductPackage();
    }
    
    @Override
    public String toString() {
        return "%s %s".formatted(product.getName(), productPackage.getCode());
    }
}
