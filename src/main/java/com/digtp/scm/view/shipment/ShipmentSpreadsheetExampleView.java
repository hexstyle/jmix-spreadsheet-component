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
import java.util.function.BiConsumer;
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
                column("date", "Date", Movement::getDate, null),
                column("track", "Track", movement -> nested(movement, "track.name"), null),
                column("terminal", "Terminal", movement -> nested(movement, "warehouse.terminal.name"), null),
                column("product", "Product", movement -> nested(movement, "product.name"), null),
                column("originPlant", "Plant", movement -> nested(movement, "originPlant.name"), null),
                column("package", "Package", movement -> nested(movement, "productPackage.name"), null),
                column("reason", "Reason", this::reasonLabel, null),
                column("volume", "Volume", Movement::getVolume,
                        (movement, value) -> movement.setVolume(toInteger(value, movement.getVolume()))),
                column("fact", "Fact", Movement::getIsFact, null)
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

    private SpreadsheetColumn<Movement> column(String id,
                                              String header,
                                              Function<Movement, Object> valueProvider,
                                              BiConsumer<Movement, Object> setter) {
        return new DefaultSpreadsheetColumn<>(
                id,
                header,
                valueProvider,
                setter,
                value -> value == null ? "" : value.toString(),
                null,
                SpreadsheetColumn.Alignment.LEFT,
                setter != null
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
