package com.example.scm.messages;

import com.example.scm.JmixSpreadsheetApplication;
import io.jmix.core.Messages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
class MessageResolutionTest {

    @Autowired
    private Messages messages;

    @ParameterizedTest
    @ValueSource(strings = {
            "com.example.scm.view.main/applicationTitle.text",
            "com.example.scm.view.main/MainView.title",
            "actions.Clone",
            "placeholders.Search",
            "com.example.scm.view.login/loginForm.errorTitle",
            "databaseUniqueConstraintViolation.IDX_SCM_USER_TRACK_TYPE_ACCESS_UNQ"
    })
    void resolvesCriticalMessages(String key) {
        String message = messages.getMessage(key, Locale.ENGLISH);
        Assertions.assertNotNull(message);
        Assertions.assertFalse(message.isBlank(), "Message must not be blank for key: " + key);
        Assertions.assertNotEquals(key, message, "Raw key returned instead of localized value for key: " + key);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "com.example.scm.view.main/applicationTitle.text",
            "actions.Clone",
            "databaseUniqueConstraintViolation.IDX_SCM_USER_TRACK_TYPE_ACCESS_UNQ"
    })
    void fallsBackToEnglishForUnsupportedLocale(String key) {
        Locale unsupportedLocale = Locale.of("fr", "FR");

        String fallbackMessage = messages.getMessage(key, unsupportedLocale);
        String englishMessage = messages.getMessage(key, Locale.ENGLISH);

        Assertions.assertEquals(englishMessage, fallbackMessage);
    }
}
