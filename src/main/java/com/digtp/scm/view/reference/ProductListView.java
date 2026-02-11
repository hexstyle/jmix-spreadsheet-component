package com.digtp.scm.view.reference;

import com.digtp.scm.entity.Product;
import com.digtp.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "products", layout = MainView.class)
@ViewController("scm_Product.list")
@ViewDescriptor(path = "/com/digtp/scm/view/reference/product-list-view.xml")
@LookupComponent("productsDataGrid")
@DialogMode(width = "48em")
public class ProductListView extends StandardListView<Product> {
}
