package com.digtp.scm.ui;

import com.digtp.scm.JmixSpreadsheetApplication;
import com.digtp.scm.portbalance.data.TestDataFactory;
import com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@UiTest
@SpringBootTest(
        classes = {JmixSpreadsheetApplication.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.core.confDir=src/test/resources"
)
class ShipmentSpreadsheetExampleViewUiTest {

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @Test
    void exampleViewOpensAndRendersFlatMovementTable() {
        TestDataFactory factory = new TestDataFactory(dataManager);
        factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheetExampleView").open();

        try {
            View<?> view = dialog.getView();
            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "movementsSpreadsheet");
            Assertions.assertThat(spreadsheetComponent).isNotNull();
            Assertions.assertThat(spreadsheetComponent.getController()).isInstanceOf(DefaultSpreadsheetController.class);

            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Assertions.assertThat(spreadsheet).isNotNull();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Date")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Reason")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Volume")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "VesselLoadingReason")).isTrue();
        } finally {
            dialog.close();
        }
    }

    private boolean spreadsheetContains(Spreadsheet spreadsheet, String expected) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            for (int colIndex = row.getFirstCellNum(); colIndex < row.getLastCellNum(); colIndex++) {
                if (colIndex < 0) {
                    continue;
                }
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    continue;
                }
                String value = switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                    default -> "";
                };
                if (expected.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
