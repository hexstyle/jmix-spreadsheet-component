package com.digtp.scm.view.shipment;

import com.digtp.scm.entity.Shipment;
import com.digtp.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "shipments", layout = MainView.class)
@ViewController("Shipment.list")
@ViewDescriptor(value = "/com/digtp/scm/view/shipment/shipment-list-view.xml", path = "/com/digtp/scm/view/shipment/shipment-list-view.xml")
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
        
        shipmentsDataGrid.addColumn(shipment -> shipment.getPlant() != null ? shipment.getPlant().getName() : null)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.plant"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(shipment -> shipment.getProduct() != null ? shipment.getProduct().getName() : null)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.product"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(shipment -> shipment.getVessel() != null ? shipment.getVessel().getName() : null)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.vessel"))
                .setResizable(true)
                .setSortable(true);
        
        shipmentsDataGrid.addColumn(Shipment::getValue)
                .setHeader(messages.getMessage("com.company.jmixspreadsheet.entity/Shipment.value"))
                .setResizable(true)
                .setSortable(true);
    }
}
