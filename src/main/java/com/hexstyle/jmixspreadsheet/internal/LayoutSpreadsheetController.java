package com.hexstyle.jmixspreadsheet.internal;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetController;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.index.DefaultLayoutIndex;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import com.hexstyle.jmixspreadsheet.render.PoiSpreadsheetRenderer;
import com.hexstyle.jmixspreadsheet.render.SpreadsheetRenderSupport;
import com.hexstyle.jmixspreadsheet.ui.component.SpreadsheetComponentConfig;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

import org.apache.poi.ss.usermodel.Cell;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Spreadsheet controller backed by a pre-built {@link SpreadsheetLayout}.
 * <p>
 * Intended for complex custom layouts such as port balance tables.
 *
 * @param <E> the cell model type
 */
public class LayoutSpreadsheetController<E> implements SpreadsheetController<E, Object> {

    private final Spreadsheet spreadsheet;
    private final Supplier<SpreadsheetLayout<E>> layoutSupplier;
    private final PoiSpreadsheetRenderer<E> renderer;
    private final Function<E, Object> cellKeyProvider;
    private final SpreadsheetComponentConfig<E> config;

    private SpreadsheetInteractionHandler<E> interactionHandler;
    private LayoutIndex<E> layoutIndex;
    private SpreadsheetLayout<E> currentLayout;
    private boolean bound;
    private boolean interactionBound;
    private boolean editListenerBound;
    private boolean readOnly = true;
    private boolean navigationGridVisible = true;

    public LayoutSpreadsheetController(Supplier<SpreadsheetLayout<E>> layoutSupplier,
                                       PoiSpreadsheetRenderer<E> renderer,
                                       Function<E, Object> cellKeyProvider,
                                       SpreadsheetComponentConfig<E> config) {
        if (layoutSupplier == null) {
            throw new IllegalArgumentException("Layout supplier cannot be null");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("Renderer cannot be null");
        }
        if (cellKeyProvider == null) {
            throw new IllegalArgumentException("Cell key provider cannot be null");
        }
        this.layoutSupplier = layoutSupplier;
        this.renderer = renderer;
        this.cellKeyProvider = cellKeyProvider;
        this.config = config;
        this.spreadsheet = new Spreadsheet();
        this.spreadsheet.setFunctionBarVisible(false);
        this.spreadsheet.setRowColHeadingsVisible(false);
        this.spreadsheet.setSheetSelectionBarVisible(false);
    }

    @Override
    public void bind(SpreadsheetTableModel<E> model, Object dataContainer) {
        if (bound) {
            throw new IllegalStateException("Controller is already bound");
        }
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        this.interactionHandler = model.getInteractionHandler();
        setupInteractionListener();
        setupEditListener();
        this.bound = true;
        reload();
    }

    @Override
    public void reload() {
        ensureBound();
        SpreadsheetLayout<E> layout = layoutSupplier.get();
        if (layout == null) {
            layout = new com.hexstyle.jmixspreadsheet.layout.DefaultSpreadsheetLayout<>(1, 1, java.util.List.of(),
                    java.util.List.of(), java.util.List.of());
        }
        this.currentLayout = layout;

        renderer.render(spreadsheet, currentLayout);
        if (config.resolveAutoResize(true)) {
            SpreadsheetRenderSupport.resizeSheet(spreadsheet, currentLayout);
        }

        SpreadsheetRenderSupport.applyHeaderColumnWidths(
                spreadsheet,
                currentLayout,
                config.resolveHeaderRowIndex(0),
                config.getHeaderWidthOverrides(),
                config.getHeaderWidthDeltas()
        );
        SpreadsheetRenderSupport.refreshGrouping(spreadsheet, currentLayout, navigationGridVisible);

        layoutIndex = new DefaultLayoutIndex<>(currentLayout, cellKeyProvider);
        renderer.flush(spreadsheet);

        if (config.getAfterRender() != null) {
            config.getAfterRender().accept(spreadsheet);
        }
        if (config.resolveAutoRefreshViewport(true)) {
            SpreadsheetRenderSupport.refreshViewportAfterLayout(spreadsheet, currentLayout);
        }
    }

    @Override
    public Spreadsheet getComponent() {
        ensureBound();
        return spreadsheet;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setNavigationGridVisible(boolean visible) {
        this.navigationGridVisible = visible;
        SpreadsheetRenderSupport.refreshGrouping(spreadsheet, currentLayout, navigationGridVisible);
    }

    @Override
    public boolean isNavigationGridVisible() {
        return navigationGridVisible;
    }

    @Override
    public void save() {
        ensureBound();
    }

    @Override
    public void updateAffectedCells(Set<Object> affectedEntityKeys) {
        reload();
    }

    private void ensureBound() {
        if (!bound) {
            throw new IllegalStateException("Controller is not bound");
        }
    }

    private void setupInteractionListener() {
        if (interactionBound || interactionHandler == null) {
            return;
        }
        spreadsheet.addSelectionChangeListener(event ->
                SpreadsheetInteractionBridge.handleSelectionChange(event, interactionHandler, layoutIndex));
        interactionBound = true;
    }

    private void setupEditListener() {
        if (editListenerBound) {
            return;
        }
        spreadsheet.addCellValueChangeListener(event -> {
            if (!readOnly) {
                return;
            }
            if (layoutIndex == null) {
                return;
            }
            var changedCells = event.getChangedCells();
            if (changedCells == null || changedCells.isEmpty()) {
                return;
            }
            for (org.apache.poi.ss.util.CellReference cellRef : changedCells) {
                int row = cellRef.getRow();
                int col = cellRef.getCol();
                revertCellValue(row, col);
            }
        });
        editListenerBound = true;
    }

    private void revertCellValue(int row, int col) {
        if (layoutIndex == null) {
            return;
        }
        LayoutIndex.CellRef cellRef = new CellRefImpl(row, col);
        CellBinding<E> binding = layoutIndex.getCellBinding(cellRef);
        if (binding == null) {
            return;
        }
        Object value = binding.getValue();
        String formattedValue = value == null ? "" : value.toString();
        Cell cell = spreadsheet.createCell(row, col, formattedValue);
        if (cell != null) {
            spreadsheet.refreshCells(cell);
        }
    }

    private static final class CellRefImpl implements LayoutIndex.CellRef {
        private final int row;
        private final int column;

        private CellRefImpl(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public int getRow() {
            return row;
        }

        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LayoutIndex.CellRef ref)) {
                return false;
            }
            return row == ref.getRow() && column == ref.getColumn();
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, column);
        }
    }
}
