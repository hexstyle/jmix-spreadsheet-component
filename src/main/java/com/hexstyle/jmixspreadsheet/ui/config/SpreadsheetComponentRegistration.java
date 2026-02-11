package com.hexstyle.jmixspreadsheet.ui.config;

import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import com.hexstyle.jmixspreadsheet.ui.loader.SpreadsheetComponentLoader;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for registering SpreadsheetComponent with Jmix FlowUI.
 * <p>
 * This configuration registers the component and its loader so that the
 * component can be used declaratively in XML view descriptors.
 */
@Configuration
public class SpreadsheetComponentRegistration {

    @Bean
    public ComponentRegistration spreadsheet() {
        return ComponentRegistrationBuilder.create(SpreadsheetComponent.class)
                .withComponentLoader("spreadsheet", SpreadsheetComponentLoader.class)
                .build();
    }
}
