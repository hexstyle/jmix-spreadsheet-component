package com.company.jmixspreadsheet.view.shipment;

import com.company.jmixspreadsheet.entity.Shipment;
import com.company.jmixspreadsheet.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import io.jmix.flowui.component.genericfilter.GenericFilter;

@Route(value = "shipments", layout = MainView.class)
@ViewController("Shipment.list")
@ViewDescriptor("shipment-list-view.xml")
@LookupComponent("shipmentsDataGrid")
@DialogMode(width = "64em")
public class ShipmentListView extends StandardListView<Shipment> {

    @ViewComponent
    private DataGrid<Shipment> shipmentsDataGrid;
    @ViewComponent
    private CollectionLoader<Shipment> shipmentsDl;

    @Autowired
    private Messages messages;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DataComponents dataComponents;

    @ViewComponent
    private GenericFilter genericFilter;

    @ViewComponent
    private PropertyFilter plantFilter;

    @Subscribe
    public void onInit(final InitEvent event) {
        setupColumns();
    }


    private void setupColumns() {
        // Configure columns programmatically in Java
        shipmentsDataGrid.removeAllColumns();
        
        shipmentsDataGrid.addColumn(Shipment::getDay)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.day"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(shipment -> shipment.getPlant() != null ? shipment.getPlant().getCaption() : null)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.plant"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(shipment -> shipment.getProduct() != null ? shipment.getProduct().getCaption() : null)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.product"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(shipment -> shipment.getVessel() != null ? shipment.getVessel().getCaption() : null)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.vessel"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(Shipment::getValue)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.value"))
                .setResizable(true)
                .setSortable(true);
    }
}
