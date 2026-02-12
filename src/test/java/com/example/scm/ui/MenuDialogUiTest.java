package com.example.scm.ui;

import com.example.scm.JmixSpreadsheetApplication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@UiTest
@SpringBootTest(
        classes = {JmixSpreadsheetApplication.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.core.confDir=src/test/resources"
)
class MenuDialogUiTest {

    @Autowired
    DialogWindows dialogWindows;

    @Test
    void menuItemsOpenInDialog() {
        View<?> origin = UiTestUtils.getCurrentView();

        assertMenuViewOpens(origin, "User.list");
        assertMenuViewOpens(origin, "Shipment.list");
        assertMenuViewOpens(origin, "ShipmentSpreadsheet.list");
    }

    private void assertMenuViewOpens(View<?> origin, String viewId) {
        DialogWindow<View<?>> dialogWindow = Assertions.assertDoesNotThrow(
                () -> dialogWindows.view(origin, viewId).open()
        );

        try {
            Assertions.assertNotNull(dialogWindow.getView());
        } finally {
            dialogWindow.close();
        }
    }
}
