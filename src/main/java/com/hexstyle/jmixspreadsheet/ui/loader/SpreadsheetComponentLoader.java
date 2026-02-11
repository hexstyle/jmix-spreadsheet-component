package com.hexstyle.jmixspreadsheet.ui.loader;

import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.springframework.stereotype.Component;

/**
 * Component loader for SpreadsheetComponent that loads XML attributes.
 * <p>
 * This loader processes XML spreadsheet elements and loads common component attributes.
 * The controller binding is handled in Java code (e.g., in the view's InitEvent handler).
 * <p>
 * The loader delegates all business logic to the controller, serving
 * as a thin adapter between XML and the controller API.
 */
@Component
public class SpreadsheetComponentLoader extends AbstractComponentLoader<SpreadsheetComponent<?>> {

    @Override
    protected SpreadsheetComponent<?> createComponent() {
        return factory.create(SpreadsheetComponent.class);
    }

    @Override
    public void loadComponent() {
        // Load common attributes
        loadId(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        loadBoolean(element, "readOnly", resultComponent::setReadOnly);
        loadBoolean(element, "read-only", resultComponent::setReadOnly);
        loadBoolean(element, "navigationGridVisible", resultComponent::setNavigationGridVisible);
        loadBoolean(element, "navigation-grid-visible", resultComponent::setNavigationGridVisible);
        loadBoolean(element, "autoRefreshViewport", resultComponent::setAutoRefreshViewport);
        loadBoolean(element, "auto-refresh-viewport", resultComponent::setAutoRefreshViewport);
        loadBoolean(element, "autoResize", resultComponent::setAutoResize);
        loadBoolean(element, "auto-resize", resultComponent::setAutoResize);
        loadString(element, "headerStyle", resultComponent::setHeaderStyle);
        loadString(element, "header-style", resultComponent::setHeaderStyle);
        loadString(element, "dataLoader", this::bindDataLoader);
        loadString(element, "data-loader", this::bindDataLoader);
        
        // Note: Controller binding is done in Java code (e.g., in view's InitEvent handler)
        // The dataContainer attribute is available via element.attributeValue("dataContainer")
        // but binding is deferred to Java code for flexibility
    }

    private void bindDataLoader(String loaderId) {
        if (loaderId == null || loaderId.isBlank()) {
            return;
        }
        CollectionLoader<?> loader = getComponentContext().getViewData().getLoader(loaderId);
        if (loader == null) {
            throw new IllegalStateException("Data loader not found: " + loaderId);
        }
        resultComponent.setDataLoader(loader);
    }
}
