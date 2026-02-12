package com.hexstyle.jmixspreadsheet.ui.component;

import com.hexstyle.jmixspreadsheet.api.DefaultSpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.ui.StyleRule;
import com.hexstyle.jmixspreadsheet.ui.StyleToken;
import io.jmix.flowui.model.CollectionContainer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Declarative configuration for {@link SpreadsheetComponent}.
 * <p>
 * Use {@link #forTable(Class, SpreadsheetTableModel, CollectionContainer)} for simple
 * flat/pivot tables, or {@link #forLayout(Class, Supplier)} for custom layouts.
 *
 * @param <E> the entity type (or cell model type for layout mode)
 */
public final class SpreadsheetComponentConfig<E> {

    public static final String DEFAULT_HEADER_STYLE =
            "background-color:#F3F4F6;color:#111827;font-weight:600;";

    public enum Mode {
        TABLE,
        LAYOUT
    }

    private final Mode mode;
    private final Class<E> entityClass;

    private SpreadsheetTableModel<E> tableModel;
    private CollectionContainer<E> dataContainer;
    private Supplier<SpreadsheetLayout<E>> layoutSupplier;
    private SpreadsheetInteractionHandler<E> interactionHandler;
    private Function<E, Object> cellKeyProvider;

    private final List<StyleRule<E>> styleRules = new ArrayList<>();
    private Function<StyleToken, String> styleProvider = token -> null;

    private Boolean readOnly;
    private Boolean navigationGridVisible;
    private Boolean autoRefreshViewport;
    private Boolean autoResize;
    private Integer headerRowIndex;
    private String headerStyle;
    private Boolean headerStyleEnabled;

    private final Map<String, Integer> headerWidthOverrides = new LinkedHashMap<>();
    private final Map<String, Integer> headerWidthDeltas = new LinkedHashMap<>();
    private Consumer<com.vaadin.flow.component.spreadsheet.Spreadsheet> afterRender;

    private SpreadsheetComponentConfig(Mode mode, Class<E> entityClass) {
        this.mode = mode;
        this.entityClass = entityClass;
    }

    public static <E> SpreadsheetComponentConfig<E> forTable(
            Class<E> entityClass,
            SpreadsheetTableModel<E> model,
            CollectionContainer<E> container) {
        SpreadsheetComponentConfig<E> config = new SpreadsheetComponentConfig<>(Mode.TABLE, entityClass);
        config.tableModel = model;
        config.dataContainer = container;
        return config;
    }

    public static <E> SpreadsheetComponentConfig<E> forLayout(
            Class<E> entityClass,
            Supplier<SpreadsheetLayout<E>> layoutSupplier) {
        SpreadsheetComponentConfig<E> config = new SpreadsheetComponentConfig<>(Mode.LAYOUT, entityClass);
        config.layoutSupplier = layoutSupplier;
        return config;
    }

    public SpreadsheetComponentConfig<E> withInteractionHandler(SpreadsheetInteractionHandler<E> handler) {
        this.interactionHandler = handler;
        return this;
    }

    public SpreadsheetComponentConfig<E> withReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public SpreadsheetComponentConfig<E> withNavigationGridVisible(boolean navigationGridVisible) {
        this.navigationGridVisible = navigationGridVisible;
        return this;
    }

    public SpreadsheetComponentConfig<E> withAutoRefreshViewport(boolean autoRefreshViewport) {
        this.autoRefreshViewport = autoRefreshViewport;
        return this;
    }

    public SpreadsheetComponentConfig<E> withAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
        return this;
    }

    public SpreadsheetComponentConfig<E> withHeaderRowIndex(int headerRowIndex) {
        this.headerRowIndex = headerRowIndex;
        return this;
    }

    public SpreadsheetComponentConfig<E> withHeaderStyle(String headerStyle) {
        this.headerStyle = headerStyle;
        return this;
    }

    public SpreadsheetComponentConfig<E> withHeaderStyleEnabled(boolean enabled) {
        this.headerStyleEnabled = enabled;
        return this;
    }

    public SpreadsheetComponentConfig<E> addHeaderWidthOverride(String header, int width) {
        this.headerWidthOverrides.put(header, width);
        return this;
    }

    public SpreadsheetComponentConfig<E> addHeaderWidthDelta(String header, int delta) {
        this.headerWidthDeltas.put(header, delta);
        return this;
    }

    public SpreadsheetComponentConfig<E> withCellKeyProvider(Function<E, Object> cellKeyProvider) {
        this.cellKeyProvider = cellKeyProvider;
        return this;
    }

    public SpreadsheetComponentConfig<E> addStyleRule(StyleRule<E> rule) {
        if (rule != null) {
            this.styleRules.add(rule);
        }
        return this;
    }

    public SpreadsheetComponentConfig<E> withStyleProvider(Function<StyleToken, String> styleProvider) {
        if (styleProvider != null) {
            this.styleProvider = styleProvider;
        }
        return this;
    }

    public SpreadsheetComponentConfig<E> afterRender(
            Consumer<com.vaadin.flow.component.spreadsheet.Spreadsheet> afterRender) {
        this.afterRender = afterRender;
        return this;
    }

    public Mode getMode() {
        return mode;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public SpreadsheetTableModel<E> resolveModel() {
        if (tableModel != null) {
            if (interactionHandler != null) {
                return new DefaultSpreadsheetTableModel<>(
                        tableModel.getEntityClass(),
                        tableModel.getColumns(),
                        tableModel.getGrouping(),
                        tableModel.getFilter(),
                        tableModel.getSort(),
                        tableModel.getPivot(),
                        interactionHandler
                );
            }
            return tableModel;
        }
        return new DefaultSpreadsheetTableModel<>(
                entityClass,
                List.of(),
                null,
                null,
                null,
                Optional.empty(),
                interactionHandler
        );
    }

    public CollectionContainer<E> getDataContainer() {
        return dataContainer;
    }

    public Supplier<SpreadsheetLayout<E>> getLayoutSupplier() {
        return layoutSupplier;
    }

    public SpreadsheetInteractionHandler<E> resolveInteractionHandler() {
        if (interactionHandler != null) {
            return interactionHandler;
        }
        return tableModel != null ? tableModel.getInteractionHandler() : null;
    }

    public Function<E, Object> resolveCellKeyProvider() {
        if (cellKeyProvider != null) {
            return cellKeyProvider;
        }
        return entity -> entity == null ? null : System.identityHashCode(entity);
    }

    public List<StyleRule<E>> getStyleRules() {
        return List.copyOf(styleRules);
    }

    public Function<StyleToken, String> getStyleProvider() {
        return styleProvider;
    }

    public boolean resolveReadOnly(boolean defaultValue) {
        return readOnly != null ? readOnly : defaultValue;
    }

    public boolean resolveNavigationGridVisible(boolean defaultValue) {
        return navigationGridVisible != null ? navigationGridVisible : defaultValue;
    }

    public boolean resolveAutoRefreshViewport(boolean defaultValue) {
        return autoRefreshViewport != null ? autoRefreshViewport : defaultValue;
    }

    public boolean resolveAutoResize(boolean defaultValue) {
        return autoResize != null ? autoResize : defaultValue;
    }

    public int resolveHeaderRowIndex(int defaultValue) {
        return headerRowIndex != null ? headerRowIndex : defaultValue;
    }

    public String resolveHeaderStyle(String defaultValue) {
        return headerStyle != null ? headerStyle : defaultValue;
    }

    public boolean resolveHeaderStyleEnabled(boolean defaultValue) {
        return headerStyleEnabled != null ? headerStyleEnabled : defaultValue;
    }

    public Map<String, Integer> getHeaderWidthOverrides() {
        return headerWidthOverrides;
    }

    public Map<String, Integer> getHeaderWidthDeltas() {
        return headerWidthDeltas;
    }

    public Consumer<com.vaadin.flow.component.spreadsheet.Spreadsheet> getAfterRender() {
        return afterRender;
    }

    void validate() {
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class is required");
        }
        if (mode == Mode.TABLE) {
            if (tableModel == null) {
                throw new IllegalArgumentException("Table model is required for table mode");
            }
            if (dataContainer == null) {
                throw new IllegalArgumentException("Data container is required for table mode");
            }
        } else {
            if (layoutSupplier == null) {
                throw new IllegalArgumentException("Layout supplier is required for layout mode");
            }
        }
    }
}
