package com.digtp.scm.view.shipment;

import com.digtp.scm.entity.Movement;
import com.digtp.scm.view.main.MainView;
import com.hexstyle.jmixspreadsheet.api.DefaultSpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.DefaultSpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponentConfig;
import com.vaadin.flow.router.Route;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.util.List;
import java.util.Optional;

@Route(value = "shipments-spreadsheet-example", layout = MainView.class)
@ViewController("ShipmentSpreadsheetExampleView")
@ViewDescriptor("shipment-spreadsheet-example.xml")
public class ShipmentSpreadsheetExampleView extends StandardView {

    @ViewComponent
    private SpreadsheetComponent<Movement> movementsSpreadsheet;

    @ViewComponent
    private CollectionContainer<Movement> movementsDc;

    @Subscribe
    public void onInit(final InitEvent event) {
        movementsSpreadsheet.configure(
                SpreadsheetComponentConfig.forTable(Movement.class, createTableModel(), movementsDc)
                        .withReadOnly(true)
        );
    }

    private SpreadsheetTableModel<Movement> createTableModel() {
        List<SpreadsheetColumn<Movement>> columns = List.of(
                column("date", "Date", Movement::getDate),
                column("track", "Track", movement -> nested(movement, "track.name")),
                column("terminal", "Terminal", movement -> nested(movement, "warehouse.terminal.name")),
                column("product", "Product", movement -> nested(movement, "product.name")),
                column("originPlant", "Plant", movement -> nested(movement, "originPlant.name")),
                column("package", "Package", movement -> nested(movement, "productPackage.name")),
                column("reason", "Reason", this::reasonLabel),
                column("volume", "Volume", Movement::getVolume),
                column("fact", "Fact", Movement::getIsFact)
        );

        return new DefaultSpreadsheetTableModel<>(
                Movement.class,
                columns,
                null,
                null,
                null,
                Optional.empty(),
                null
        );
    }

    private SpreadsheetColumn<Movement> column(String id, String header,
                                               java.util.function.Function<Movement, Object> valueProvider) {
        return new DefaultSpreadsheetColumn<>(
                id,
                header,
                valueProvider,
                null,
                value -> value == null ? "" : value.toString(),
                null,
                SpreadsheetColumn.Alignment.LEFT,
                false
        );
    }


    private Object reasonLabel(Movement movement) {
        if (movement == null || movement.getReason() == null) {
            return null;
        }
        return movement.getReason().getClass().getSimpleName();
    }

    private Object nested(Movement movement, String propertyPath) {
        if (movement == null) {
            return null;
        }
        return EntityValues.getValueEx(movement, propertyPath);
    }
}
