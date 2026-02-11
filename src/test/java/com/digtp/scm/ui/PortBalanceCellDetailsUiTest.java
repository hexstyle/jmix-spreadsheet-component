package com.digtp.scm.ui;

import com.digtp.scm.JmixSpreadsheetApplication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@UiTest
@SpringBootTest(
        classes = {JmixSpreadsheetApplication.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.core.confDir=src/test/resources"
)
class PortBalanceCellDetailsUiTest {

    @Autowired
    private DialogWindows dialogWindows;

    @Test
    void opensDetailsDialog() {
        View<?> origin = UiTestUtils.getCurrentView();
        DialogWindow<View<?>> dialog = dialogWindows
                .view(origin, "PortBalanceCellDetails.view")
                .open();

        try {
            View<?> view = dialog.getView();
            Assertions.assertNotNull(UiTestUtils.getComponent(view, "plantShipmentGrid"));
            Assertions.assertNotNull(UiTestUtils.getComponent(view, "vesselLoadItemGrid"));
            Assertions.assertNotNull(UiTestUtils.getComponent(view, "vesselLoadGrid"));
        } finally {
            dialog.close();
        }
    }
}
