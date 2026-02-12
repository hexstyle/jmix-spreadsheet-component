package com.digtp.scm.ui;

import com.digtp.scm.JmixSpreadsheetApplication;
import com.digtp.scm.portbalance.columns.ComboKey;
import com.digtp.scm.portbalance.columns.PortBalanceMetric;
import com.digtp.scm.portbalance.columns.TrackKey;
import com.digtp.scm.portbalance.data.TestDataFactory;
import com.digtp.scm.portbalance.data.TestDataFactory.January2026PortBalanceData;
import com.digtp.scm.portbalance.layout.PortBalanceCellContext;
import com.hexstyle.jmixspreadsheet.internal.LayoutSpreadsheetController;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import org.assertj.core.api.Assertions;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UiTest
@SpringBootTest(
        classes = {JmixSpreadsheetApplication.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.core.confDir=src/test/resources"
)
class ShipmentSpreadsheetViewUiTest {

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Messages messages;

    @Test
    void viewWiresPortBalanceComponents() {
        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();

            SpreadsheetComponent<?> spreadsheet = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            Assertions.assertThat(spreadsheet).isNotNull();
            Object controller = spreadsheet.getController();
            Assertions.assertThat(controller).isNotNull();
            Assertions.assertThat(controller.getClass().getName())
                    .isEqualTo(LayoutSpreadsheetController.class.getName());

            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            LogicalFilterComponent<?> root = filter.getCurrentConfiguration().getRootLogicalFilterComponent();
            List<PropertyFilter> propertyFilters = root.getFilterComponents().stream()
                    .filter(PropertyFilter.class::isInstance)
                    .map(component -> (PropertyFilter) component)
                    .toList();

            Assertions.assertThat(propertyFilters).hasSize(7);
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter -> {
                Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("date");
                Assertions.assertThat(propertyFilter.getOperation())
                        .isEqualTo(PropertyFilter.Operation.GREATER_OR_EQUAL);
            });
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter -> {
                Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("date");
                Assertions.assertThat(propertyFilter.getOperation())
                        .isEqualTo(PropertyFilter.Operation.LESS_OR_EQUAL);
            });
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter ->
                    Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("track"));
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter ->
                    Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("warehouse.terminal"));
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter ->
                    Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("product"));
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter ->
                    Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("originPlant"));
            Assertions.assertThat(propertyFilters).anySatisfy(propertyFilter ->
                    Assertions.assertThat(propertyFilter.getProperty()).isEqualTo("productPackage"));

            MultiSelectComboBox<PortBalanceMetric> metrics =
                    UiTestUtils.getComponent(view, "metricsSelector");
            Assertions.assertThat(metrics).isNotNull();
            Assertions.assertThat(metrics.getValue()).containsExactlyInAnyOrder(
                    PortBalanceMetric.IN,
                    PortBalanceMetric.STOCK,
                    PortBalanceMetric.OUT
            );
            metrics.setValue(EnumSet.of(
                    PortBalanceMetric.IN,
                    PortBalanceMetric.OUT
            ));
            Assertions.assertThat(metrics.getValue())
                    .containsExactlyInAnyOrder(PortBalanceMetric.IN, PortBalanceMetric.OUT);
        } finally {
            dialog.close();
        }
    }

    @Test
    void viewRendersJanuaryPortBalanceDataAndResolvesMessages() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            String viewTitle = view.getElement().getProperty("title");
            String resolvedTitle = messages.getMessage("com.digtp.scm.view.shipment/ShipmentSpreadsheetView.title");
            if (viewTitle != null && !viewTitle.isBlank()) {
                Assertions.assertThat(viewTitle).isEqualTo(resolvedTitle);
                Assertions.assertThat(viewTitle).doesNotContain("msg://");
            } else {
                Assertions.assertThat(resolvedTitle).doesNotContain("msg://");
            }

            Anchor baseLink = null;
            try {
                baseLink = UiTestUtils.getComponent(origin, "baseLink");
            } catch (IllegalArgumentException ignored) {
                // MainView may not be the current view in UI tests.
            }
            if (baseLink != null) {
                Assertions.assertThat(baseLink.getText())
                        .isEqualTo(messages.getMessage("com.digtp.scm.view.main/applicationTitle.text"));
                Assertions.assertThat(baseLink.getText()).doesNotContain("msg://");
            }

            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 10));
            trackFilter.setValue(data.track());
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            spreadsheetComponent.reload();

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();

            Assertions.assertThat(spreadsheetContains(spreadsheet, "Vessel")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Laycan")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Total Out")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, data.multiVesselCount() + " vessels")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "2026-01-04")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.inVolume1()))).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.outVolume()))).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.negativeStockValue()))).isTrue();

            Assertions.assertThat(hasGroupedRows(spreadsheet)).isTrue();
            Assertions.assertThat(spreadsheet.isRowColHeadingsVisible()).isFalse();

            Cell negativeStockCell = findCell(spreadsheet, String.valueOf(data.negativeStockValue()));
            Assertions.assertThat(negativeStockCell).isNotNull();
            Assertions.assertThat(isNegativeStockStyled(negativeStockCell)).isTrue();
        } finally {
            dialog.close();
        }
    }

    @Test
    void januaryFilterShowsDataAndExpectedColumns() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            filter.setAutoApply(false);

            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 31));
            trackFilter.setValue(data.track());
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.inVolume1()))).isFalse();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.outVolume()))).isFalse();
            filter.getDataLoader().load();

            Assertions.assertThat(spreadsheetContains(spreadsheet, "Vessel")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Laycan")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Total Out")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "IN")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "OUT")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "STOCK")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.inVolume1()))).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, String.valueOf(data.outVolume()))).isTrue();
        } finally {
            dialog.close();
        }
    }

    @Test
    void metricSelectionRedrawsSpreadsheetColumns() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }

            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 10));
            trackFilter.setValue(data.track());
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            MultiSelectComboBox<PortBalanceMetric> metrics =
                    UiTestUtils.getComponent(view, "metricsSelector");

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN));
            spreadsheetComponent.reload();

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();
            Assertions.assertThat(rowContains(spreadsheet, 2, "IN")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "OUT")).isFalse();
            Assertions.assertThat(rowContains(spreadsheet, 2, "STOCK")).isFalse();
        } finally {
            dialog.close();
        }
    }

    @Test
    void removingStockKeepsSecondLevelTrackHeadersAndCorrectGrouping() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            filter.setAutoApply(false);

            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 10));
            trackFilter.setValue(null);
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            MultiSelectComboBox<PortBalanceMetric> metrics =
                    UiTestUtils.getComponent(view, "metricsSelector");
            filter.getDataLoader().load();

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();
            Assertions.assertThat(rowContains(spreadsheet, 2, "STOCK")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "IN")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "OUT")).isTrue();

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN, PortBalanceMetric.OUT));
            spreadsheetComponent.reload();

            Assertions.assertThat(rowContains(spreadsheet, 2, "STOCK")).isFalse();
            assertTrackHeaderGrouping(spreadsheet, "PB26 Track BASIC");
            assertTrackHeaderGrouping(spreadsheet, "PB26 Track Plan PLANNING");
            assertClientMergedRegionsInSync(spreadsheet);
        } finally {
            dialog.close();
        }
    }

    @Test
    void singleMetricAcrossTracksKeepsSecondLevelHeadersStable() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            filter.setAutoApply(false);

            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 10));
            trackFilter.setValue(null);
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            MultiSelectComboBox<PortBalanceMetric> metrics =
                    UiTestUtils.getComponent(view, "metricsSelector");
            filter.getDataLoader().load();

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN));
            spreadsheetComponent.reload();

            Assertions.assertThat(rowContains(spreadsheet, 2, "IN")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "OUT")).isFalse();
            Assertions.assertThat(rowContains(spreadsheet, 2, "STOCK")).isFalse();
            assertSingleMetricTrackHeaderGrouping(spreadsheet, "PB26 Track BASIC", "IN");
            assertSingleMetricTrackHeaderGrouping(spreadsheet, "PB26 Track Plan PLANNING", "IN");
            assertClientMergedRegionsInSync(spreadsheet);
        } finally {
            dialog.close();
        }
    }

    @Test
    void reAddingMetricsExpandsSpreadsheetColumnCount() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            filter.setAutoApply(false);

            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 10));
            trackFilter.setValue(null);
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            MultiSelectComboBox<PortBalanceMetric> metrics =
                    UiTestUtils.getComponent(view, "metricsSelector");
            filter.getDataLoader().load();

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN));
            spreadsheetComponent.reload();
            int reducedMaxUsedColumn = maxNonEmptyColumnIndex(spreadsheet);

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN, PortBalanceMetric.OUT, PortBalanceMetric.STOCK));
            spreadsheetComponent.reload();
            int expandedMaxUsedColumn = maxNonEmptyColumnIndex(spreadsheet);

            Assertions.assertThat(expandedMaxUsedColumn)
                    .as("Workbook used area should expand when metrics are added back")
                    .isGreaterThan(reducedMaxUsedColumn);
            Assertions.assertThat(rowContains(spreadsheet, 2, "OUT")).isTrue();
            Assertions.assertThat(rowContains(spreadsheet, 2, "STOCK")).isTrue();
        } finally {
            dialog.close();
        }
    }

    @Test
    void reAddingMetricsExpandsSheetBoundsToCoverWorkbookCells() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheet.list").open();

        try {
            View<?> view = dialog.getView();
            GenericFilter filter = UiTestUtils.getComponent(view, "genericFilter");
            Assertions.assertThat(filter).isNotNull();

            if (filter.getCurrentConfiguration() == filter.getEmptyConfiguration()) {
                var defaultConfiguration = filter.getConfiguration("defaultConfiguration");
                if (defaultConfiguration != null) {
                    filter.setCurrentConfiguration(defaultConfiguration);
                }
            }
            filter.setAutoApply(false);

            PropertyFilter<LocalDate> dateFromFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.GREATER_OR_EQUAL));
            PropertyFilter<LocalDate> dateToFilter = castFilter(findPropertyFilter(
                    filter, "date", PropertyFilter.Operation.LESS_OR_EQUAL));
            PropertyFilter<com.digtp.scm.entity.Track> trackFilter = castFilter(
                    findPropertyFilter(filter, "track", PropertyFilter.Operation.EQUAL));
            PropertyFilter<com.digtp.scm.entity.Terminal> terminalFilter = castFilter(
                    findPropertyFilter(filter, "warehouse.terminal", PropertyFilter.Operation.EQUAL));

            dateFromFilter.setValue(LocalDate.of(2026, 1, 1));
            dateToFilter.setValue(LocalDate.of(2026, 1, 10));
            trackFilter.setValue(null);
            terminalFilter.setValue(data.terminal());

            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "shipmentsSpreadsheet");
            MultiSelectComboBox<PortBalanceMetric> metrics =
                    UiTestUtils.getComponent(view, "metricsSelector");
            filter.getDataLoader().load();

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN));
            spreadsheetComponent.reload();

            metrics.setValue(EnumSet.of(PortBalanceMetric.IN, PortBalanceMetric.OUT, PortBalanceMetric.STOCK));
            spreadsheetComponent.reload();

            assertSheetBoundsCoverWorkbook(spreadsheet);
            assertInternalSizeStateCoversVisibleArea(spreadsheet);
        } finally {
            dialog.close();
        }
    }

    @Test
    void lookupViewsAreAvailableForFilters() {
        View<?> origin = UiTestUtils.getCurrentView();

        DialogWindow<View<?>> trackDialog = dialogWindows.view(origin, "scm_Track.list").open();
        trackDialog.close();

        DialogWindow<View<?>> terminalDialog = dialogWindows.view(origin, "scm_Terminal.list").open();
        terminalDialog.close();

        DialogWindow<View<?>> plantDialog = dialogWindows.view(origin, "scm_Plant.list").open();
        plantDialog.close();

        DialogWindow<View<?>> productDialog = dialogWindows.view(origin, "scm_Product.list").open();
        productDialog.close();

        DialogWindow<View<?>> packageDialog = dialogWindows.view(origin, "scm_ProductPackage.list").open();
        packageDialog.close();
    }

    @Test
    void cellDetailsViewLoadsTerminalCode() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        PortBalanceCellContext context = new PortBalanceCellContext(
                LocalDate.of(2026, 1, 6),
                TrackKey.from(data.track()),
                data.terminal(),
                data.transportType(),
                PortBalanceMetric.OUT,
                null,
                ComboKey.from(data.combination()),
                false,
                false,
                null,
                null
        );

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "PortBalanceCellDetails.view")
                .withViewConfigurer(view -> setCellDetailsContext(view, context))
                .open();

        try {
            View<?> view = dialog.getView();
            DataGrid<com.digtp.scm.entity.VesselLoad> grid = UiTestUtils.getComponent(
                    view, "vesselLoadGrid");
            Assertions.assertThat(grid.getItems().getItems()).isNotEmpty();
            grid.getItems().getItems().forEach(load ->
                    Assertions.assertThat(load.getTerminal().getCode()).isNotBlank());
        } finally {
            dialog.close();
        }
    }

    @Test
    void cellDetailsViewShowsOnlyInMovements() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        PortBalanceCellContext context = new PortBalanceCellContext(
                LocalDate.of(2026, 1, 4),
                TrackKey.from(data.track()),
                data.terminal(),
                data.transportType(),
                PortBalanceMetric.IN,
                null,
                ComboKey.from(data.combination()),
                false,
                false,
                null,
                null
        );

        DialogWindow<View<?>> dialog = openCellDetails(context);
        try {
            DataGrid<com.digtp.scm.entity.PlantShipmentReason> plantGrid =
                    UiTestUtils.getComponent(dialog.getView(), "plantShipmentGrid");
            DataGrid<com.digtp.scm.entity.VesselLoadItem> outGrid =
                    UiTestUtils.getComponent(dialog.getView(), "vesselLoadItemGrid");
            DataGrid<com.digtp.scm.entity.VesselLoad> vesselGrid =
                    UiTestUtils.getComponent(dialog.getView(), "vesselLoadGrid");

            Assertions.assertThat(plantGrid.isVisible()).isTrue();
            Assertions.assertThat(outGrid.isVisible()).isFalse();
            Assertions.assertThat(vesselGrid.isVisible()).isFalse();
            Assertions.assertThat(plantGrid.getItems().getItems()).isNotEmpty();
            Assertions.assertThat(outGrid.getItems().getItems()).isEmpty();
            Assertions.assertThat(vesselGrid.getItems().getItems()).isEmpty();
        } finally {
            dialog.close();
        }
    }

    @Test
    void cellDetailsViewShowsOnlyOutMovements() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        PortBalanceCellContext context = new PortBalanceCellContext(
                LocalDate.of(2026, 1, 6),
                TrackKey.from(data.track()),
                data.terminal(),
                data.transportType(),
                PortBalanceMetric.OUT,
                null,
                ComboKey.from(data.combination()),
                false,
                false,
                null,
                null
        );

        DialogWindow<View<?>> dialog = openCellDetails(context);
        try {
            DataGrid<com.digtp.scm.entity.PlantShipmentReason> plantGrid =
                    UiTestUtils.getComponent(dialog.getView(), "plantShipmentGrid");
            DataGrid<com.digtp.scm.entity.VesselLoadItem> outGrid =
                    UiTestUtils.getComponent(dialog.getView(), "vesselLoadItemGrid");
            DataGrid<com.digtp.scm.entity.VesselLoad> vesselGrid =
                    UiTestUtils.getComponent(dialog.getView(), "vesselLoadGrid");

            Assertions.assertThat(plantGrid.isVisible()).isFalse();
            Assertions.assertThat(outGrid.isVisible()).isTrue();
            Assertions.assertThat(vesselGrid.isVisible()).isTrue();
            Assertions.assertThat(plantGrid.getItems().getItems()).isEmpty();
            Assertions.assertThat(outGrid.getItems().getItems()).isNotEmpty();
            Assertions.assertThat(vesselGrid.getItems().getItems()).isNotEmpty();
        } finally {
            dialog.close();
        }
    }

    @Test
    void cellDetailsViewUsesVesselLoadSelectionForItems() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        January2026PortBalanceData data = factory.ensureJanuary2026PortBalanceData();

        PortBalanceCellContext context = new PortBalanceCellContext(
                LocalDate.of(2026, 1, 9),
                TrackKey.from(data.track()),
                data.terminal(),
                data.transportType(),
                PortBalanceMetric.OUT,
                null,
                ComboKey.from(data.combination()),
                false,
                false,
                null,
                null
        );

        DialogWindow<View<?>> dialog = openCellDetails(context);
        try {
            DataGrid<com.digtp.scm.entity.VesselLoad> vesselGrid =
                    UiTestUtils.getComponent(dialog.getView(), "vesselLoadGrid");
            DataGrid<com.digtp.scm.entity.VesselLoadItem> itemGrid =
                    UiTestUtils.getComponent(dialog.getView(), "vesselLoadItemGrid");

            List<com.digtp.scm.entity.VesselLoad> loads = vesselGrid.getItems().getItems().stream().toList();
            Assertions.assertThat(loads).hasSizeGreaterThanOrEqualTo(2);

            com.digtp.scm.entity.VesselLoad first = loads.get(0);
            vesselGrid.select(first);
            Assertions.assertThat(itemGrid.getItems().getItems())
                    .allSatisfy(item -> Assertions.assertThat(item.getVesselLoad().getId())
                            .isEqualTo(first.getId()));

            com.digtp.scm.entity.VesselLoad second = loads.get(1);
            vesselGrid.select(second);
            Assertions.assertThat(itemGrid.getItems().getItems())
                    .allSatisfy(item -> Assertions.assertThat(item.getVesselLoad().getId())
                            .isEqualTo(second.getId()));
        } finally {
            dialog.close();
        }
    }

    private void setCellDetailsContext(View<?> view, PortBalanceCellContext context) {
        try {
            var method = view.getClass().getMethod("setContext", PortBalanceCellContext.class);
            method.invoke(view, context);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set port balance cell context", e);
        }
    }

    private DialogWindow<View<?>> openCellDetails(PortBalanceCellContext context) {
        View<?> origin = UiTestUtils.getCurrentView();
        return dialogWindows.view(origin, "PortBalanceCellDetails.view")
                .withViewConfigurer(view -> setCellDetailsContext(view, context))
                .open();
    }

    private PropertyFilter<?> findPropertyFilter(GenericFilter filter,
                                                 String property,
                                                 PropertyFilter.Operation operation) {
        LogicalFilterComponent<?> root = filter.getCurrentConfiguration().getRootLogicalFilterComponent();
        return root.getFilterComponents().stream()
                .filter(PropertyFilter.class::isInstance)
                .map(component -> (PropertyFilter<?>) component)
                .filter(component -> property.equals(component.getProperty()))
                .filter(component -> operation == component.getOperation())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "PropertyFilter not found for " + property + " / " + operation));
    }

    @SuppressWarnings("unchecked")
    private <T> PropertyFilter<T> castFilter(PropertyFilter<?> filter) {
        return (PropertyFilter<T>) filter;
    }

    private boolean spreadsheetContains(Spreadsheet spreadsheet, String expected) {
        if (spreadsheet == null || spreadsheet.getWorkbook() == null) {
            return false;
        }
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                String value = switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                    default -> null;
                };
                if (expected.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasGroupedRows(Spreadsheet spreadsheet) {
        if (spreadsheet == null || spreadsheet.getWorkbook() == null) {
            return false;
        }
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            if (row.getOutlineLevel() > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean rowContains(Spreadsheet spreadsheet, int rowIndex, String expected) {
        if (spreadsheet == null || spreadsheet.getWorkbook() == null) {
            return false;
        }
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return false;
        }
        for (org.apache.poi.ss.usermodel.Cell cell : row) {
            String value = switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> null;
            };
            if (expected.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private int maxNonEmptyColumnIndex(Spreadsheet spreadsheet) {
        if (spreadsheet == null || spreadsheet.getWorkbook() == null) {
            return -1;
        }
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        int maxColumn = -1;
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                    continue;
                }
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
                        && (cell.getStringCellValue() == null || cell.getStringCellValue().isBlank())) {
                    continue;
                }
                maxColumn = Math.max(maxColumn, cell.getColumnIndex());
            }
        }
        return maxColumn;
    }

    private void assertTrackHeaderGrouping(Spreadsheet spreadsheet, String trackLabel) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        org.apache.poi.ss.usermodel.Row trackHeaderRow = sheet.getRow(1);
        Assertions.assertThat(trackHeaderRow).isNotNull();

        List<Integer> headerColumns = new ArrayList<>();
        for (org.apache.poi.ss.usermodel.Cell cell : trackHeaderRow) {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
                    && trackLabel.equals(cell.getStringCellValue())) {
                headerColumns.add(cell.getColumnIndex());
            }
        }

        Assertions.assertThat(headerColumns)
                .as("track header '" + trackLabel + "' must be present")
                .isNotEmpty();

        for (Integer startColumn : headerColumns) {
            Assertions.assertThat(cellText(sheet, 2, startColumn)).isEqualTo("IN");
            Assertions.assertThat(cellText(sheet, 2, startColumn + 1)).isEqualTo("OUT");

            Integer mergedLastColumn = mergedRegionLastColumn(sheet, 1, 1, startColumn);
            Assertions.assertThat(mergedLastColumn)
                    .as("second-level track group width for '" + trackLabel + "'")
                    .isEqualTo(startColumn + 1);
        }
    }

    private void assertSingleMetricTrackHeaderGrouping(Spreadsheet spreadsheet,
                                                       String trackLabel,
                                                       String metricCaption) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        org.apache.poi.ss.usermodel.Row trackHeaderRow = sheet.getRow(1);
        Assertions.assertThat(trackHeaderRow).isNotNull();

        List<Integer> headerColumns = new ArrayList<>();
        for (org.apache.poi.ss.usermodel.Cell cell : trackHeaderRow) {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
                    && trackLabel.equals(cell.getStringCellValue())) {
                headerColumns.add(cell.getColumnIndex());
            }
        }

        Assertions.assertThat(headerColumns)
                .as("track header '" + trackLabel + "' must be present in single metric mode")
                .isNotEmpty();

        for (Integer startColumn : headerColumns) {
            Assertions.assertThat(cellText(sheet, 2, startColumn)).isEqualTo(metricCaption);
            Integer mergedLastColumn = mergedRegionLastColumn(sheet, 1, 1, startColumn);
            Assertions.assertThat(mergedLastColumn)
                    .as("single metric mode should not merge track header '" + trackLabel + "' with neighbor")
                    .isNull();
        }
    }

    private Integer mergedRegionLastColumn(Sheet sheet, int firstRow, int lastRow, int firstColumn) {
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (region.getFirstRow() == firstRow
                    && region.getLastRow() == lastRow
                    && region.getFirstColumn() == firstColumn) {
                return region.getLastColumn();
            }
        }
        return null;
    }

    private void assertClientMergedRegionsInSync(Spreadsheet spreadsheet) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        Set<String> workbookRegions = new HashSet<>();
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            workbookRegions.add(region.getFirstRow() + ":" + region.getLastRow()
                    + ":" + region.getFirstColumn() + ":" + region.getLastColumn());
        }

        Set<String> componentRegions = new HashSet<>();
        for (Object region : readComponentMergedRegions(spreadsheet)) {
            int row1 = readMergedField(region, "row1") - 1;
            int row2 = readMergedField(region, "row2") - 1;
            int col1 = readMergedField(region, "col1") - 1;
            int col2 = readMergedField(region, "col2") - 1;
            componentRegions.add(row1 + ":" + row2 + ":" + col1 + ":" + col2);
        }

        Assertions.assertThat(componentRegions)
                .as("Spreadsheet merged region state must match workbook merged regions")
                .isEqualTo(workbookRegions);
    }

    private void assertSheetBoundsCoverWorkbook(Spreadsheet spreadsheet) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        int workbookMaxRow = -1;
        int workbookMaxCol = -1;
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                    continue;
                }
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
                        && (cell.getStringCellValue() == null || cell.getStringCellValue().isBlank())) {
                    continue;
                }
                workbookMaxRow = Math.max(workbookMaxRow, cell.getRowIndex());
                workbookMaxCol = Math.max(workbookMaxCol, cell.getColumnIndex());
            }
        }
        Assertions.assertThat(workbookMaxRow).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(workbookMaxCol).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(spreadsheet.getColumns())
                .as("Spreadsheet visible column bound must include workbook right boundary")
                .isGreaterThanOrEqualTo(workbookMaxCol + 1);
        Assertions.assertThat(spreadsheet.getRows())
                .as("Spreadsheet visible row bound must include workbook bottom boundary")
                .isGreaterThanOrEqualTo(workbookMaxRow + 1);
    }

    private void assertInternalSizeStateCoversVisibleArea(Spreadsheet spreadsheet) {
        try {
            Field colWField = Spreadsheet.class.getDeclaredField("colW");
            colWField.setAccessible(true);
            Object colWValue = colWField.get(spreadsheet);
            Assertions.assertThat(colWValue).isInstanceOf(int[].class);
            int[] colW = (int[]) colWValue;
            Assertions.assertThat(colW.length)
                    .as("Internal column width array must cover visible column area")
                    .isGreaterThanOrEqualTo(spreadsheet.getColumns());

            Field rowHField = Spreadsheet.class.getDeclaredField("rowH");
            rowHField.setAccessible(true);
            Object rowHValue = rowHField.get(spreadsheet);
            Assertions.assertThat(rowHValue).isInstanceOf(float[].class);
            float[] rowH = (float[]) rowHValue;
            Assertions.assertThat(rowH.length)
                    .as("Internal row height array must cover visible row area")
                    .isGreaterThanOrEqualTo(spreadsheet.getRows());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read internal spreadsheet size state", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> readComponentMergedRegions(Spreadsheet spreadsheet) {
        try {
            Field field = Spreadsheet.class.getDeclaredField("mergedRegions");
            field.setAccessible(true);
            Object value = field.get(spreadsheet);
            if (value == null) {
                return List.of();
            }
            if (value instanceof List<?>) {
                return (List<Object>) value;
            }
            throw new IllegalStateException("Unexpected mergedRegions type: " + value.getClass().getName());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read merged regions from Spreadsheet", e);
        }
    }

    private int readMergedField(Object region, String fieldName) {
        try {
            Field field = region.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(region);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read merged region field: " + fieldName, e);
        }
    }

    private String cellText(Sheet sheet, int rowIndex, int columnIndex) {
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Cell findCell(Spreadsheet spreadsheet, String expected) {
        if (spreadsheet == null || spreadsheet.getWorkbook() == null) {
            return null;
        }
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                String value = switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                    default -> null;
                };
                if (expected.equals(value)) {
                    return cell;
                }
            }
        }
        return null;
    }

    private boolean isNegativeStockStyled(Cell cell) {
        if (cell == null) {
            return false;
        }
        if (!(cell.getCellStyle() instanceof XSSFCellStyle style)) {
            return false;
        }
        var fill = style.getFillForegroundColorColor();
        if (fill == null || fill.getRGB() == null) {
            return false;
        }
        byte[] rgb = fill.getRGB();
        return (rgb[0] & 0xFF) == 0xFE
                && (rgb[1] & 0xFF) == 0xE2
                && (rgb[2] & 0xFF) == 0xE2;
    }
}
