package com.digtp.scm.config;

import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FlowuiViewControllersConfiguration {

    @Bean
    public ViewControllersConfiguration viewControllersConfiguration(
            ApplicationContext applicationContext,
            AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ViewControllersConfiguration configuration =
                new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        configuration.setBasePackages(List.of("com.digtp.scm.view"));
        return configuration;
    }
}
