package com.company.jmixspreadsheet.view.shipment;

import com.company.jmixspreadsheet.entity.Shipment;
import com.company.jmixspreadsheet.entity.Plant;
import com.company.jmixspreadsheet.entity.Product;
import com.company.jmixspreadsheet.entity.Vessel;
import com.company.jmixspreadsheet.view.main.MainView;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

import java.util.Arrays;

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

    @Subscribe
    public void onInit(final InitEvent event) {
        // Configure enum fields programmatically in Java
        plantField.setItems(Arrays.asList(Plant.values()));
        plantField.setItemLabelGenerator(Plant::getCaption);
        
        productField.setItems(Arrays.asList(Product.values()));
        productField.setItemLabelGenerator(Product::getCaption);
        
        vesselField.setItems(Arrays.asList(Vessel.values()));
        vesselField.setItemLabelGenerator(Vessel::getCaption);
    }
}
