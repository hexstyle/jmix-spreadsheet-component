package com.hexstyle.jmixspreadsheet.ui;

import com.hexstyle.jmixspreadsheet.diff.LayoutDelta;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetController;
import com.hexstyle.jmixspreadsheet.datasource.ContainerSpreadsheetDataSource;
import com.hexstyle.jmixspreadsheet.datasource.DataSourceAdapter;
import com.hexstyle.jmixspreadsheet.diff.DefaultChangeAnalyzer;
import com.hexstyle.jmixspreadsheet.diff.PatchApplier;
import com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController;
import com.hexstyle.jmixspreadsheet.layout.FlatTableLayoutBuilder;
import com.hexstyle.jmixspreadsheet.layout.PivotLayoutBuilder;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.render.PoiSpreadsheetRenderer;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponentConfig;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Factory for creating SpreadsheetController instances with all required dependencies.
 * <p>
 * This factory creates controllers configured with appropriate factories for
 * data source adapters, layout engines, renderers, change analyzers, and patch appliers.
 */
@Component
public class SpreadsheetControllerFactory {

    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private Metadata metadata;

    /**
     * Creates a spreadsheet controller for the given entity class.
     * <p>
     * The controller is created with a new Vaadin Spreadsheet component and
     * configured with all necessary factories. The controller uses DataManager
     * for entity persistence and Metadata for entity creation.
     *
     * @param entityClass the entity class
     * @return the controller instance
     */
    public <E> SpreadsheetController<E, CollectionContainer<E>> createController(
            Class<E> entityClass) {

        // Create Vaadin Spreadsheet component
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setFunctionBarVisible(false);
        spreadsheet.setRowColHeadingsVisible(false);
        spreadsheet.setSheetSelectionBarVisible(false);

        // Entity key provider (default: use entity ID)
        Function<E, Object> entityKeyProvider = EntityValues::getId;

        // Create factories
        DefaultSpreadsheetController.DataSourceAdapterFactory<E> dataSourceAdapterFactory =
                container -> createDataSourceAdapter((CollectionContainer<E>) container, entityKeyProvider);

        DefaultSpreadsheetController.LayoutEngineFactory<E> layoutEngineFactory =
                model -> model.getPivot().isPresent()
                        ? new PivotLayoutBuilder<>()
                        : new FlatTableLayoutBuilder<>(
                                SpreadsheetComponentConfig.DEFAULT_HEADER_STYLE,
                                true);

        DefaultSpreadsheetController.SpreadsheetRendererFactory<E, Spreadsheet> rendererFactory =
                model -> new PoiSpreadsheetRenderer<>(com.hexstyle.jmixspreadsheet.ui.SpreadsheetComponentOptions.empty());

        DefaultSpreadsheetController.ChangeAnalyzerFactory<E> changeAnalyzerFactory =
                model -> new DefaultChangeAnalyzer<>(model, entityKeyProvider);

        DefaultSpreadsheetController.PatchApplierFactory<E, Spreadsheet> patchApplierFactory =
                model -> new DefaultPatchApplier<>(); // No-op patch applier (forces full render)

        // Create controller
        return new DefaultSpreadsheetController<>(
                spreadsheet,
                dataManager,
                metadata,
                dataSourceAdapterFactory,
                layoutEngineFactory,
                rendererFactory,
                changeAnalyzerFactory,
                patchApplierFactory);
    }

    /**
     * Creates a data source adapter for a CollectionContainer.
     */
    private <E> DataSourceAdapter<E> createDataSourceAdapter(
            CollectionContainer<E> container,
            Function<E, Object> entityKeyProvider) {

        ContainerSpreadsheetDataSource.ContainerAdapter<CollectionContainer<E>, E> containerAdapter =
                new ContainerSpreadsheetDataSource.ContainerAdapter<CollectionContainer<E>, E>() {
                    @Override
                    public Iterable<E> getItems(CollectionContainer<E> c) {
                        return c.getItems();
                    }

                    @Override
                    public void subscribe(
                            CollectionContainer<E> c,
                            java.util.function.Consumer<E> itemAddedHandler,
                            java.util.function.Consumer<E> itemRemovedHandler,
                            java.util.function.Consumer<E> itemChangedHandler,
                            Runnable refreshHandler) {
                        c.addItemChangeListener(event -> {
                            if (event.getItem() != null) {
                                itemChangedHandler.accept(event.getItem());
                            }
                        });
                        c.addCollectionChangeListener(event -> {
                            // CollectionChangeEvent provides change type and affected items
                            // This listener handles all collection changes including:
                            // - Filter changes (triggers SET_ITEMS or REFRESH)
                            // - Data reloads (triggers SET_ITEMS or REFRESH)
                            // - Item additions/removals
                            var changeType = event.getChangeType();
                            java.util.Collection<? extends E> changes = event.getChanges();
                            
                            // Use string comparison to avoid import issues
                            String changeTypeName = changeType.name();
                            if ("ADD_ITEMS".equals(changeTypeName)) {
                                changes.forEach(itemAddedHandler);
                            } else if ("REMOVE_ITEMS".equals(changeTypeName)) {
                                changes.forEach(itemRemovedHandler);
                            } else if ("REFRESH".equals(changeTypeName) || "SET_ITEMS".equals(changeTypeName)) {
                                // Collection was fully refreshed (e.g., filter changed, data reloaded)
                                // This is triggered when:
                                // - GenericFilter applies a new filter and CollectionLoader loads new data
                                // - CollectionLoader.load() is called directly
                                // - Any operation that replaces the entire collection
                                // Trigger full rerender via refreshHandler
                                refreshHandler.run();
                            }
                        });
                    }

                    @Override
                    public void unsubscribe(CollectionContainer<E> c) {
                        // Container listeners are automatically cleaned up when container is disposed
                    }
                };

        return new ContainerSpreadsheetDataSource<>(container, containerAdapter);
    }

    /**
     * Placeholder for patch applier.
     */
    private static class DefaultPatchApplier<E, C> implements PatchApplier<E, C> {
        @Override
        public boolean applyPatch(C component, LayoutDelta delta,
                                  SpreadsheetLayout<E> currentLayout) {
            // For now, return false to trigger full rerender
            return false;
        }
    }
}
