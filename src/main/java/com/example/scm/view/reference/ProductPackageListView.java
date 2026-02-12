package com.example.scm.view.reference;

import com.example.scm.entity.ProductPackage;
import com.example.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "product-packages", layout = MainView.class)
@ViewController("scm_ProductPackage.list")
@ViewDescriptor(path = "/com/example/scm/view/reference/product-package-list-view.xml")
@LookupComponent("productPackagesDataGrid")
@DialogMode(width = "48em")
public class ProductPackageListView extends StandardListView<ProductPackage> {
}
