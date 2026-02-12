package com.digtp.scm.view.shipment;

import com.digtp.scm.entity.Movement;
import com.digtp.scm.view.main.MainView;
import com.hexstyle.jmixspreadsheet.api.DefaultSpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.DefaultSpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.ui.StyleRule;
import com.hexstyle.jmixspreadsheet.ui.StyleToken;
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
import java.util.function.Function;

@Route(value = "shipments-spreadsheet-example", layout = MainView.class)
@ViewController("ShipmentSpreadsheetExampleView")
@ViewDescriptor("shipment-spreadsheet-example.xml")
public class ShipmentSpreadsheetExampleView extends StandardView {

    private static final int VOLUME_COLUMN_INDEX = 7;

    @ViewComponent
    private SpreadsheetComponent<Movement> movementsSpreadsheet;

    @ViewComponent
    private CollectionContainer<Movement> movementsDc;

    @Subscribe
    public void onInit(final InitEvent event) {
        movementsSpreadsheet.configure(
                SpreadsheetComponentConfig.forTable(Movement.class, createTableModel(), movementsDc)
                        .withReadOnly(false)
                        .addStyleRule(StyleRule.of(StyleToken.HIGHLIGHT,
                                context -> context.getRowIndex() > 0 && context.getColumnIndex() == VOLUME_COLUMN_INDEX))
                        .withStyleProvider(token -> token == StyleToken.HIGHLIGHT ? "color:#7C3AED;" : null)
        );
    }

    private SpreadsheetTableModel<Movement> createTableModel() {
        List<SpreadsheetColumn<Movement>> columns = List.of(
                readOnlyColumn("date", "Date", Movement::getDate),
                readOnlyColumn("track", "Track", movement -> nested(movement, "track.name")),
                readOnlyColumn("terminal", "Terminal", movement -> nested(movement, "warehouse.terminal.name")),
                readOnlyColumn("product", "Product", movement -> nested(movement, "product.name")),
                readOnlyColumn("originPlant", "Plant", movement -> nested(movement, "originPlant.name")),
                readOnlyColumn("package", "Package", movement -> nested(movement, "productPackage.name")),
                readOnlyColumn("reason", "Reason", this::reasonLabel),
                editableVolumeColumn(),
                readOnlyColumn("fact", "Fact", Movement::getIsFact)
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

    private SpreadsheetColumn<Movement> readOnlyColumn(String id, String header,
                                                        Function<Movement, Object> valueProvider) {
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

    private SpreadsheetColumn<Movement> editableVolumeColumn() {
        return new DefaultSpreadsheetColumn<>(
                "volume",
                "Volume",
                Movement::getVolume,
                (movement, value) -> movement.setVolume(toInteger(value, movement.getVolume())),
                value -> value == null ? "" : value.toString(),
                null,
                SpreadsheetColumn.Alignment.LEFT,
                true
        );
    }

    private Integer toInteger(Object value, Integer fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return fallback;
        }
        return Integer.parseInt(text);
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
