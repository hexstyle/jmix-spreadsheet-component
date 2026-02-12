package com.hexstyle.jmixspreadsheet.ui;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetInteractionHandler;
import com.hexstyle.jmixspreadsheet.index.DefaultLayoutIndex;
import com.hexstyle.jmixspreadsheet.index.LayoutIndex;
import com.hexstyle.jmixspreadsheet.internal.InteractionContextImpl;
import com.hexstyle.jmixspreadsheet.internal.SpreadsheetInteractionBridge;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.DefaultCellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import org.apache.poi.ss.util.CellReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class SpreadsheetInteractionHandlerTest {

    @Test
    void clickBridgeBuildsContextFromBinding() {
        DefaultCellBinding<String> binding =
                new DefaultCellBinding<>(2, 3, "value", null, "entity-1");
        SpreadsheetLayout<String> layout = new TestLayout<>(List.of(binding));
        LayoutIndex<String> index = new DefaultLayoutIndex<>(layout, entity -> entity);

        List<SpreadsheetInteractionHandler.InteractionContext<String>> contexts = new ArrayList<>();
        SpreadsheetInteractionHandler<String> handler = new SpreadsheetInteractionHandler<>() {
            @Override
            public void onCellClick(InteractionContext<String> context) {
                contexts.add(context);
            }
        };

        Set<CellReference> selectedCells = Set.of(
                new CellReference(2, (short) 3),
                new CellReference(5, (short) 1)
        );

        SpreadsheetInteractionBridge.handleCellClick(2, 3, selectedCells, handler, index);

        Assertions.assertThat(contexts).hasSize(1);
        SpreadsheetInteractionHandler.InteractionContext<String> context = contexts.get(0);
        Assertions.assertThat(context.getRowIndex()).isEqualTo(2);
        Assertions.assertThat(context.getColumnIndex()).isEqualTo(3);
        Assertions.assertThat(context.getEntity()).isEqualTo("entity-1");
        Assertions.assertThat(context.isPivotCell()).isFalse();
        Assertions.assertThat(context.getSelectedRows()).containsExactly(2, 5);
        Assertions.assertThat(context.getSelectedColumns()).containsExactly(1, 3);

        Assertions.assertThat(context).isInstanceOf(InteractionContextImpl.class);
        InteractionContextImpl<String> impl = (InteractionContextImpl<String>) context;
        Assertions.assertThat(impl.getCellBinding()).isSameAs(binding);
    }



    @Test
    void editBridgeProvidesEditMetadata() {
        DefaultCellBinding<String> binding =
                new DefaultCellBinding<>(1, 1, "old", null, "entity-2");
        SpreadsheetLayout<String> layout = new TestLayout<>(List.of(binding));
        LayoutIndex<String> index = new DefaultLayoutIndex<>(layout, entity -> entity);

        List<SpreadsheetInteractionHandler.InteractionContext<String>> contexts = new ArrayList<>();
        SpreadsheetInteractionHandler<String> handler = new SpreadsheetInteractionHandler<>() {
            @Override
            public void onCellEdit(InteractionContext<String> context) {
                contexts.add(context);
            }
        };

        Instant ts = Instant.parse("2026-02-12T10:15:30Z");
        SpreadsheetInteractionBridge.handleCellEdit(1, 1, handler, index, ts, "old", "123", true, null);

        Assertions.assertThat(contexts).hasSize(1);
        SpreadsheetInteractionHandler.InteractionContext<String> context = contexts.get(0);
        Assertions.assertThat(context.getEntity()).isEqualTo("entity-2");
        Assertions.assertThat(context.getEditTimestamp()).isEqualTo(ts);
        Assertions.assertThat(context.getOldValue()).isEqualTo("old");
        Assertions.assertThat(context.getNewValue()).isEqualTo("123");
        Assertions.assertThat(context.isEditSuccessful()).isTrue();
        Assertions.assertThat(context.getEditError()).isNull();
    }
    private static final class TestLayout<E> implements SpreadsheetLayout<E> {
        private final List<CellBinding<E>> bindings;

        private TestLayout(List<CellBinding<E>> bindings) {
            this.bindings = bindings;
        }

        @Override
        public int getRowCount() {
            return 10;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public List<CellBinding<E>> getCellBindings() {
            return bindings;
        }

        @Override
        public List<MergedRegion> getMergedRegions() {
            return List.of();
        }

        @Override
        public List<RowGroup> getRowGroups() {
            return List.of();
        }
    }
}
