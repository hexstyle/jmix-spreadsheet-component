package com.hexstyle.jmixspreadsheet.ui.component;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetController;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.model.CollectionLoader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Jmix FlowUI component wrapper for Vaadin Spreadsheet.
 * <p>
 * This component wraps the Vaadin Spreadsheet component and delegates all logic
 * to the existing SpreadsheetController. It contains no business logic and serves
 * as a thin adapter layer between FlowUI XML and the controller API.
 * <p>
 * The component is designed to be used declaratively in XML views with data binding.
 *
 * @param <E> the entity type
 */
public class SpreadsheetComponent<E> extends Composite<Div> implements HasSize {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    private SpreadsheetController<E, ?> controller;
    private Spreadsheet spreadsheet;
    private boolean readOnly = true;
    private boolean navigationGridVisible = true;
    private boolean autoRefreshViewport = true;
    private boolean autoResize = true;
    private String headerStyle = SpreadsheetComponentConfig.DEFAULT_HEADER_STYLE;
    private Subscription dataLoaderPostLoadRegistration;
    private boolean dataLoaderReloadPending;

    /**
     * Creates a new spreadsheet component.
     */
    public SpreadsheetComponent() {
        // Component will be initialized via setController()
    }

    @Override
    protected Div initContent() {
        Div content = super.initContent();
        return content;
    }

    /**
     * Sets the spreadsheet controller that manages this component.
     * <p>
     * This method should be called by the component loader during initialization.
     * The controller manages all business logic and data binding.
     *
     * @param controller the spreadsheet controller
     */
    public void setController(SpreadsheetController<E, ?> controller) {
        this.controller = controller;
        getContent().removeAll();
        if (controller == null) {
            this.spreadsheet = null;
            return;
        }
        this.spreadsheet = controller.getComponent();
        controller.setReadOnly(readOnly);
        controller.setNavigationGridVisible(navigationGridVisible);
        // Attach the Vaadin Spreadsheet component to the content div
        getContent().add(spreadsheet);
        if (dataLoaderReloadPending) {
            dataLoaderReloadPending = false;
            reload();
        }
    }

    /**
     * Returns the spreadsheet controller.
     *
     * @return the controller, or {@code null} if not set
     */
    public SpreadsheetController<E, ?> getController() {
        return controller;
    }

    /**
     * Returns the underlying Vaadin Spreadsheet component.
     * <p>
     * This method provides access to the wrapped Vaadin component.
     * The component is available after the controller has been set and bound.
     *
     * @return the Vaadin Spreadsheet component, or {@code null} if not yet initialized
     */
    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    /**
     * Returns whether the spreadsheet is read-only.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets read-only mode for the spreadsheet.
     * <p>
     * When enabled, any cell edits are rejected and reverted to their previous values.
     *
     * @param readOnly whether the spreadsheet is read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (controller != null) {
            controller.setReadOnly(readOnly);
        }
    }

    /**
     * Returns whether navigation grid (row/column headings) is visible.
     *
     * @return {@code true} if navigation grid is visible
     */
    public boolean isNavigationGridVisible() {
        return navigationGridVisible;
    }

    /**
     * Sets navigation grid (row/column headings) visibility.
     *
     * @param navigationGridVisible whether navigation grid should be visible
     */
    public void setNavigationGridVisible(boolean navigationGridVisible) {
        this.navigationGridVisible = navigationGridVisible;
        if (controller != null) {
            controller.setNavigationGridVisible(navigationGridVisible);
        }
    }

    /**
     * Returns whether viewport refresh is enabled after reloads.
     */
    public boolean isAutoRefreshViewport() {
        return autoRefreshViewport;
    }

    /**
     * Enables or disables viewport refresh after reloads.
     */
    public void setAutoRefreshViewport(boolean autoRefreshViewport) {
        this.autoRefreshViewport = autoRefreshViewport;
    }

    /**
     * Returns whether the spreadsheet is resized to the layout size after reloads.
     */
    public boolean isAutoResize() {
        return autoResize;
    }

    /**
     * Enables or disables spreadsheet resize to the layout size after reloads.
     */
    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }

    /**
     * Returns the default header style for flat table layouts.
     */
    public String getHeaderStyle() {
        return headerStyle;
    }

    /**
     * Sets the default header style for flat table layouts.
     */
    public void setHeaderStyle(String headerStyle) {
        if (headerStyle != null) {
            this.headerStyle = headerStyle;
        }
    }

    /**
     * Configures the component using a single settings object.
     *
     * @param config configuration for table or layout mode
     */
    public void configure(SpreadsheetComponentConfig<E> config) {
        if (config == null) {
            throw new IllegalArgumentException("Spreadsheet config cannot be null");
        }
        config.validate();

        boolean resolvedReadOnly = config.resolveReadOnly(readOnly);
        boolean resolvedNavigation = config.resolveNavigationGridVisible(navigationGridVisible);
        boolean resolvedAutoRefresh = config.resolveAutoRefreshViewport(autoRefreshViewport);
        boolean resolvedAutoResize = config.resolveAutoResize(autoResize);
        String resolvedHeaderStyle = config.resolveHeaderStyle(headerStyle);

        this.readOnly = resolvedReadOnly;
        this.navigationGridVisible = resolvedNavigation;
        this.autoRefreshViewport = resolvedAutoRefresh;
        this.autoResize = resolvedAutoResize;
        this.headerStyle = resolvedHeaderStyle;
        config.withReadOnly(resolvedReadOnly)
                .withNavigationGridVisible(resolvedNavigation)
                .withAutoRefreshViewport(resolvedAutoRefresh)
                .withAutoResize(resolvedAutoResize)
                .withHeaderStyle(resolvedHeaderStyle);

        SpreadsheetController<E, ?> newController = SpreadsheetComponentFactory.createController(
                config,
                resolvedReadOnly,
                resolvedNavigation,
                resolvedAutoRefresh,
                resolvedAutoResize,
                resolvedHeaderStyle,
                dataManager,
                metadata
        );
        Object dataContainer = config.getMode() == SpreadsheetComponentConfig.Mode.TABLE
                ? config.getDataContainer()
                : new Object();
        @SuppressWarnings("unchecked")
        SpreadsheetController<E, Object> typedController = (SpreadsheetController<E, Object>) newController;
        typedController.bind(config.resolveModel(), dataContainer);
        setController(typedController);
    }

    /**
     * Reloads the spreadsheet data.
     * <p>
     * This method delegates to the controller's reload() method.
     *
     * @throws IllegalStateException if the controller is not bound
     */
    public void reload() {
        if (controller == null) {
            throw new IllegalStateException("Controller is not set");
        }
        controller.reload();
    }

    /**
     * Binds component reload to the post-load lifecycle of a data loader.
     * <p>
     * This allows declarative integration with components such as GenericFilter
     * that trigger data loads automatically.
     */
    public void setDataLoader(CollectionLoader<?> dataLoader) {
        if (dataLoaderPostLoadRegistration != null) {
            dataLoaderPostLoadRegistration.remove();
            dataLoaderPostLoadRegistration = null;
        }
        if (dataLoader == null) {
            return;
        }
        dataLoaderPostLoadRegistration = dataLoader.addPostLoadListener(event -> {
            if (controller == null) {
                dataLoaderReloadPending = true;
                return;
            }
            reload();
        });
    }

    /**
     * Factory methods for creating controllers based on configuration.
     */
    private static final class SpreadsheetComponentFactory {
        private SpreadsheetComponentFactory() {
        }

        private static <E> SpreadsheetController<E, ?> createController(
                SpreadsheetComponentConfig<E> config,
                boolean readOnly,
                boolean navigationGridVisible,
                boolean autoRefreshViewport,
                boolean autoResize,
                String headerStyle,
                DataManager dataManager,
                Metadata metadata) {
            if (config.getMode() == SpreadsheetComponentConfig.Mode.LAYOUT) {
                var options = new com.hexstyle.jmixspreadsheet.ui.SpreadsheetComponentOptions<>(
                        config.getStyleRules(),
                        config.getStyleProvider()
                );
                var renderer = new com.hexstyle.jmixspreadsheet.render.PoiSpreadsheetRenderer<>(options);
                var controller = new com.hexstyle.jmixspreadsheet.internal.LayoutSpreadsheetController<>(
                        config.getLayoutSupplier(),
                        renderer,
                        config.resolveCellKeyProvider(),
                        config
                );
                controller.setReadOnly(readOnly);
                controller.setNavigationGridVisible(navigationGridVisible);
                return controller;
            }

            var spreadsheet = new Spreadsheet();
            spreadsheet.setFunctionBarVisible(false);
            spreadsheet.setRowColHeadingsVisible(false);
            spreadsheet.setSheetSelectionBarVisible(false);

            var options = new com.hexstyle.jmixspreadsheet.ui.SpreadsheetComponentOptions<>(
                    config.getStyleRules(),
                    config.getStyleProvider()
            );

            if (dataManager == null || metadata == null) {
                throw new IllegalStateException("DataManager and Metadata are required for table mode");
            }
            var layoutEngineFactory = new com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController.LayoutEngineFactory<E>() {
                @Override
                public com.hexstyle.jmixspreadsheet.layout.LayoutEngine<E> create(
                        com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel<E> model) {
                    return model.getPivot().isPresent()
                            ? new com.hexstyle.jmixspreadsheet.layout.PivotLayoutBuilder<>()
                            : new com.hexstyle.jmixspreadsheet.layout.FlatTableLayoutBuilder<>(
                            headerStyle,
                            config.resolveHeaderStyleEnabled(true)
                    );
                }
            };

            var rendererFactory = new com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController.SpreadsheetRendererFactory<E, Spreadsheet>() {
                @Override
                public com.hexstyle.jmixspreadsheet.render.SpreadsheetRenderer<E, Spreadsheet> create(
                        com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel<E> model) {
                    return new com.hexstyle.jmixspreadsheet.render.PoiSpreadsheetRenderer<>(options);
                }
            };

            var dataSourceAdapterFactory = new com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController.DataSourceAdapterFactory<E>() {
                @Override
                public com.hexstyle.jmixspreadsheet.datasource.DataSourceAdapter<E> create(Object container) {
                    if (!(container instanceof io.jmix.flowui.model.CollectionContainer<?>)) {
                        throw new IllegalArgumentException("Unsupported data container: " + container);
                    }
                    @SuppressWarnings("unchecked")
                    io.jmix.flowui.model.CollectionContainer<E> cast =
                            (io.jmix.flowui.model.CollectionContainer<E>) container;
                    com.hexstyle.jmixspreadsheet.datasource.ContainerSpreadsheetDataSource.ContainerAdapter<
                            io.jmix.flowui.model.CollectionContainer<E>, E> containerAdapter =
                            new com.hexstyle.jmixspreadsheet.datasource.ContainerSpreadsheetDataSource.ContainerAdapter<>() {
                                @Override
                                public Iterable<E> getItems(io.jmix.flowui.model.CollectionContainer<E> c) {
                                    return c.getItems();
                                }

                                @Override
                                public void subscribe(io.jmix.flowui.model.CollectionContainer<E> c,
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
                                        String changeTypeName = event.getChangeType().name();
                                        java.util.Collection<? extends E> changes = event.getChanges();
                                        if ("ADD_ITEMS".equals(changeTypeName)) {
                                            changes.forEach(itemAddedHandler);
                                        } else if ("REMOVE_ITEMS".equals(changeTypeName)) {
                                            changes.forEach(itemRemovedHandler);
                                        } else if ("REFRESH".equals(changeTypeName) || "SET_ITEMS".equals(changeTypeName)) {
                                            refreshHandler.run();
                                        }
                                    });
                                }

                                @Override
                                public void unsubscribe(io.jmix.flowui.model.CollectionContainer<E> c) {
                                    // Container listeners are managed by the container lifecycle.
                                }
                            };
                    return new com.hexstyle.jmixspreadsheet.datasource.ContainerSpreadsheetDataSource<>(cast, containerAdapter);
                }
            };

            var changeAnalyzerFactory = new com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController.ChangeAnalyzerFactory<E>() {
                @Override
                public com.hexstyle.jmixspreadsheet.diff.ChangeAnalyzer<E> create(
                        com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel<E> model) {
                    return new com.hexstyle.jmixspreadsheet.diff.DefaultChangeAnalyzer<>(
                            model,
                            io.jmix.core.entity.EntityValues::getId
                    );
                }
            };

            var patchApplierFactory = new com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController.PatchApplierFactory<E, Spreadsheet>() {
                @Override
                public com.hexstyle.jmixspreadsheet.diff.PatchApplier<E, Spreadsheet> create(
                        com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel<E> model) {
                    return new com.hexstyle.jmixspreadsheet.diff.PatchApplier<>() {
                        @Override
                        public boolean applyPatch(Spreadsheet component,
                                                  com.hexstyle.jmixspreadsheet.diff.LayoutDelta delta,
                                                  com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout<E> currentLayout) {
                            return false;
                        }
                    };
                }
            };

            var postRenderHook = new java.util.function.BiConsumer<Spreadsheet, com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout<E>>() {
                @Override
                public void accept(Spreadsheet component,
                                   com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout<E> layout) {
                    if (autoResize) {
                        com.hexstyle.jmixspreadsheet.render.SpreadsheetRenderSupport.resizeSheet(component, layout);
                    }
                    if (config.getMode() == SpreadsheetComponentConfig.Mode.TABLE) {
                        java.util.List<com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn<E>> columns =
                                config.resolveModel().getColumns();
                        for (int i = 0; i < columns.size(); i++) {
                            Integer width = columns.get(i).getWidth();
                            if (width != null) {
                                component.setColumnWidth(i, width);
                            }
                        }
                    }
                    com.hexstyle.jmixspreadsheet.render.SpreadsheetRenderSupport.applyHeaderColumnWidths(
                            component,
                            layout,
                            config.resolveHeaderRowIndex(0),
                            config.getHeaderWidthOverrides(),
                            config.getHeaderWidthDeltas()
                    );
                    com.hexstyle.jmixspreadsheet.render.SpreadsheetRenderSupport.refreshGrouping(
                            component,
                            layout,
                            navigationGridVisible
                    );
                    if (config.getAfterRender() != null) {
                        config.getAfterRender().accept(component);
                    }
                    if (autoRefreshViewport) {
                        com.hexstyle.jmixspreadsheet.render.SpreadsheetRenderSupport.refreshViewport(component, layout);
                    }
                }
            };

            var controller = new com.hexstyle.jmixspreadsheet.internal.DefaultSpreadsheetController<>(
                    spreadsheet,
                    dataManager,
                    metadata,
                    dataSourceAdapterFactory,
                    layoutEngineFactory,
                    rendererFactory,
                    changeAnalyzerFactory,
                    patchApplierFactory,
                    postRenderHook
            );
            controller.setReadOnly(readOnly);
            controller.setNavigationGridVisible(navigationGridVisible);
            return controller;
        }
    }
}
