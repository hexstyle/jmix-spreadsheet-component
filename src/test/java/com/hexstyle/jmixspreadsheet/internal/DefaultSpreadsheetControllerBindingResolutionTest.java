package com.hexstyle.jmixspreadsheet.internal;

import com.hexstyle.jmixspreadsheet.index.DefaultLayoutIndex;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.DefaultCellBinding;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

class DefaultSpreadsheetControllerBindingResolutionTest {

    @Test
    void resolveCellBindingDoesNotFallbackToShiftedCoordinates() throws Exception {
        DefaultCellBinding<String> binding =
                new DefaultCellBinding<>(0, 0, "value", null, "entity-1");
        SpreadsheetLayout<String> layout = new TestLayout<>(List.of(binding));

        DefaultSpreadsheetController<String, Object> controller = new DefaultSpreadsheetController<>(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Field layoutIndexField = DefaultSpreadsheetController.class.getDeclaredField("layoutIndex");
        layoutIndexField.setAccessible(true);
        layoutIndexField.set(controller, new DefaultLayoutIndex<>(layout, entity -> entity));

        Method resolveCellBinding = DefaultSpreadsheetController.class
                .getDeclaredMethod("resolveCellBinding", int.class, int.class);
        resolveCellBinding.setAccessible(true);

        Object resolution = resolveCellBinding.invoke(controller, 1, 1);

        Method bindingAccessor = resolution.getClass().getDeclaredMethod("binding");
        bindingAccessor.setAccessible(true);
        Method rowAccessor = resolution.getClass().getDeclaredMethod("row");
        rowAccessor.setAccessible(true);
        Method colAccessor = resolution.getClass().getDeclaredMethod("col");
        colAccessor.setAccessible(true);

        Assertions.assertThat(bindingAccessor.invoke(resolution)).isNull();
        Assertions.assertThat(rowAccessor.invoke(resolution)).isEqualTo(1);
        Assertions.assertThat(colAccessor.invoke(resolution)).isEqualTo(1);
    }

    private static final class TestLayout<E> implements SpreadsheetLayout<E> {
        private final List<CellBinding<E>> bindings;

        private TestLayout(List<CellBinding<E>> bindings) {
            this.bindings = bindings;
        }

        @Override
        public int getRowCount() {
            return 3;
        }

        @Override
        public int getColumnCount() {
            return 3;
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
