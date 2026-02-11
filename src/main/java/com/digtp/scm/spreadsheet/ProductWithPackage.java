package com.digtp.scm.spreadsheet;

import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.ShippingCombination;
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
