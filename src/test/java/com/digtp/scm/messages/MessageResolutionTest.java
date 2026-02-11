package com.digtp.scm.messages;

import com.digtp.scm.JmixSpreadsheetApplication;
import io.jmix.core.Messages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
class MessageResolutionTest {

    @Autowired
    private Messages messages;

    @Test
    void resolvesApplicationTitleText() {
        String message = messages.getMessage("com.digtp.scm.view.main/applicationTitle.text", Locale.ENGLISH);
        Assertions.assertEquals("Calendar Planning", message);
    }
}
