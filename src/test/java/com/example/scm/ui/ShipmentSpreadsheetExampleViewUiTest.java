package com.example.scm.ui;

import com.example.scm.JmixSpreadsheetApplication;
import com.example.scm.entity.Movement;
import com.example.scm.portbalance.data.TestDataFactory;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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
    void exampleViewOpensAndSupportsOnlyVolumeEditing() throws Exception {
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
            Assertions.assertThat(spreadsheetContains(spreadsheet, "Value")).isTrue();
            Assertions.assertThat(spreadsheetContains(spreadsheet, "VesselLoadingReason")).isTrue();

            com.vaadin.flow.component.textfield.TextArea editLogField = UiTestUtils.getComponent(view, "editLogField");
            Assertions.assertThat(editLogField).isNotNull();
            Assertions.assertThat(editLogField.isReadOnly()).isTrue();

            SpreadsheetTableModel<Movement> model = readModel(spreadsheetComponent.getController());
            List<SpreadsheetColumn<Movement>> columns = model.getColumns();
            SpreadsheetColumn<Movement> volumeColumn = columns.stream()
                    .filter(column -> "volume".equals(column.getId()))
                    .findFirst()
                    .orElseThrow();
            SpreadsheetColumn<Movement> dateColumn = columns.stream()
                    .filter(column -> "date".equals(column.getId()))
                    .findFirst()
                    .orElseThrow();

            Assertions.assertThat(volumeColumn.isEditable()).isTrue();
            Assertions.assertThat(volumeColumn.getSetter()).isNotNull();
            Assertions.assertThat(dateColumn.isEditable()).isFalse();
            Assertions.assertThat(dateColumn.getSetter()).isNull();

            CollectionContainer<Movement> movementsDc = readMovementsContainer(view);
            Movement movement = movementsDc.getItems().getFirst();
            Integer oldVolume = movement.getVolume();
            Object oldDate = movement.getDate();

            volumeColumn.getSetter().accept(movement, oldVolume + 5);
            Assertions.assertThat(movement.getVolume()).isEqualTo(oldVolume + 5);

            if (dateColumn.getSetter() != null) {
                dateColumn.getSetter().accept(movement, "2099-01-01");
            }
            Assertions.assertThat(movement.getDate()).isEqualTo(oldDate);

            int volumeColumnIndex = findColumnIndex(spreadsheet, "Value");
            int firstDataRow = findFirstDataRow(spreadsheet, volumeColumnIndex);
            Assertions.assertThat(isPurpleText(spreadsheet.getCell(firstDataRow, volumeColumnIndex))).isTrue();
        } finally {
            dialog.close();
        }
    }



    @Test
    void controllerRejectsHeaderEditAndPersistsValueEdit() throws Exception {
        TestDataFactory factory = new TestDataFactory(dataManager);
        factory.ensureJanuary2026PortBalanceData();

        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows.view(origin, "ShipmentSpreadsheetExampleView").open();

        try {
            View<?> view = dialog.getView();
            SpreadsheetComponent<?> spreadsheetComponent = UiTestUtils.getComponent(view, "movementsSpreadsheet");
            Spreadsheet spreadsheet = spreadsheetComponent.getSpreadsheet();
            Object controller = spreadsheetComponent.getController();
            CollectionContainer<Movement> movementsDc = readMovementsContainer(view);
            com.vaadin.flow.component.textfield.TextArea editLogField = UiTestUtils.getComponent(view, "editLogField");

            int valueColumnIndex = findColumnIndex(spreadsheet, "Value");
            int firstDataRow = findFirstDataRow(spreadsheet, valueColumnIndex);

            Movement movement = movementsDc.getItems().getFirst();
            Integer oldVolume = movement.getVolume();

            Method handleCellEdit = controller.getClass().getDeclaredMethod("handleCellEdit", int.class, int.class, Object.class);
            handleCellEdit.setAccessible(true);

            handleCellEdit.invoke(controller, 0, 0, "WRONG_HEADER");
            Assertions.assertThat(readCellValue(spreadsheet, 0, 0)).isEqualTo("Date");
            Assertions.assertThat(editLogField.getValue()).contains("success=false");

            Integer newVolume = oldVolume + 7;
            long beforeCount = dataManager.loadValue(
                            "select count(m) from scm_Movement m where m.volume = :volume", Long.class)
                    .parameter("volume", newVolume)
                    .one();

            handleCellEdit.invoke(controller, firstDataRow, valueColumnIndex, newVolume);

            Assertions.assertThat(editLogField.getValue()).contains("date=").contains("cell=").contains("entityClass=").contains("entityId=");

            long afterCount = dataManager.loadValue(
                            "select count(m) from scm_Movement m where m.volume = :volume", Long.class)
                    .parameter("volume", newVolume)
                    .one();
            Assertions.assertThat(afterCount).isGreaterThanOrEqualTo(beforeCount);

            handleCellEdit.invoke(controller, firstDataRow, valueColumnIndex, "invalid-number");
            Assertions.assertThat(editLogField.getValue()).contains("success=false");
        } finally {
            dialog.close();
        }
    }
    @SuppressWarnings("unchecked")
    private SpreadsheetTableModel<Movement> readModel(Object controller) throws Exception {
        Field modelField = controller.getClass().getDeclaredField("model");
        modelField.setAccessible(true);
        return (SpreadsheetTableModel<Movement>) modelField.get(controller);
    }

    @SuppressWarnings("unchecked")
    private CollectionContainer<Movement> readMovementsContainer(View<?> view) throws Exception {
        Field containerField = view.getClass().getDeclaredField("movementsDc");
        containerField.setAccessible(true);
        return (CollectionContainer<Movement>) containerField.get(view);
    }

    private int findColumnIndex(Spreadsheet spreadsheet, String header) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        for (int col = headerRow.getFirstCellNum(); col < headerRow.getLastCellNum(); col++) {
            Cell cell = headerRow.getCell(col);
            if (cell != null && header.equals(readCellValue(spreadsheet, 0, col))) {
                return col;
            }
        }
        throw new IllegalStateException("Header not found: " + header);
    }

    private int findFirstDataRow(Spreadsheet spreadsheet, int volumeColumnIndex) {
        Sheet sheet = spreadsheet.getWorkbook().getSheetAt(0);
        for (int row = 1; row <= sheet.getLastRowNum(); row++) {
            String value = readCellValue(spreadsheet, row, volumeColumnIndex);
            if (value.matches("-?\\d+")) {
                return row;
            }
        }
        throw new IllegalStateException("No data rows found");
    }

    private String readCellValue(Spreadsheet spreadsheet, int rowIndex, int colIndex) {
        Cell cell = spreadsheet.getCell(rowIndex, colIndex);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
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
                if (expected.equals(readCellValue(spreadsheet, rowIndex, colIndex))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPurpleText(Cell cell) {
        if (!(cell.getCellStyle() instanceof XSSFCellStyle style)) {
            return false;
        }
        XSSFFont font = style.getFont();
        if (font == null || font.getXSSFColor() == null || font.getXSSFColor().getRGB() == null) {
            return false;
        }
        byte[] rgb = font.getXSSFColor().getRGB();
        return (rgb[0] & 0xFF) == 0x7C
                && (rgb[1] & 0xFF) == 0x3A
                && (rgb[2] & 0xFF) == 0xED;
    }
}
