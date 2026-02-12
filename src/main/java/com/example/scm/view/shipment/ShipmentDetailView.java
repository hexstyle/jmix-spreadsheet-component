package com.example.scm.view.shipment;

import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.Shipment;
import com.example.scm.entity.Vessel;
import com.example.scm.view.main.MainView;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "shipments/:id", layout = MainView.class)
@ViewController("Shipment.detail")
@ViewDescriptor(value = "/com/example/scm/view/shipment/shipment-detail-view.xml", path = "/com/example/scm/view/shipment/shipment-detail-view.xml")
@EditedEntityContainer("shipmentDc")
public class ShipmentDetailView extends StandardDetailView<Shipment> {

    @ViewComponent
    private ComboBox<Plant> plantField;
    @ViewComponent
    private ComboBox<Product> productField;
    @ViewComponent
    private ComboBox<Vessel> vesselField;

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(final InitEvent event) {
        // Load entities from database
        plantField.setItems(dataManager.load(Plant.class).all().list());
        plantField.setItemLabelGenerator(Plant::getName);
        
        productField.setItems(dataManager.load(Product.class).all().list());
        productField.setItemLabelGenerator(Product::getName);
        
        vesselField.setItems(dataManager.load(Vessel.class).all().list());
        vesselField.setItemLabelGenerator(Vessel::getName);
    }
}
