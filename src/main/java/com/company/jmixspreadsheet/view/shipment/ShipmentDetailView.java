package com.company.jmixspreadsheet.view.shipment;

import com.company.jmixspreadsheet.entity.Shipment;
import com.company.jmixspreadsheet.entity.Plant;
import com.company.jmixspreadsheet.entity.Product;
import com.company.jmixspreadsheet.entity.Vessel;
import com.company.jmixspreadsheet.view.main.MainView;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "shipments/:id", layout = MainView.class)
@ViewController("Shipment.detail")
@ViewDescriptor("shipment-detail-view.xml")
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
