package com.hexstyle.jmixspreadsheet.layout;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetColumn;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Builds a flat table layout with a header row and optional row grouping.
 *
 * @param <E> the entity type
 */
public class FlatTableLayoutBuilder<E> implements LayoutEngine<E> {

    private final String headerStyle;
    private final boolean headerStyleEnabled;

    public FlatTableLayoutBuilder(String headerStyle, boolean headerStyleEnabled) {
        this.headerStyle = headerStyle;
        this.headerStyleEnabled = headerStyleEnabled;
    }

    @Override
    public SpreadsheetLayout<E> buildLayout(SpreadsheetTableModel<E> model, Iterable<E> entities) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (entities == null) {
            throw new IllegalArgumentException("Entities cannot be null");
        }

        List<SpreadsheetColumn<E>> columns = model.getColumns();
        List<E> entityList = new ArrayList<>();
        entities.forEach(entityList::add);

        int headerRows = 1;
        int rowCount = headerRows + entityList.size();
        int columnCount = columns.size();

        List<CellBinding<E>> bindings = new ArrayList<>();
        List<MergedRegion> mergedRegions = List.of();
        List<RowGroup> rowGroups = new ArrayList<>();

        buildHeaderRow(columns, bindings);
        buildDataRows(columns, entityList, bindings, headerRows);
        buildRowGroups(model, columns, entityList, rowGroups, headerRows);

        return new DefaultSpreadsheetLayout<>(rowCount, columnCount, bindings, mergedRegions, rowGroups);
    }

    private void buildHeaderRow(List<SpreadsheetColumn<E>> columns, List<CellBinding<E>> bindings) {
        if (columns == null) {
            return;
        }
        String style = headerStyleEnabled ? headerStyle : null;
        for (int col = 0; col < columns.size(); col++) {
            SpreadsheetColumn<E> column = columns.get(col);
            bindings.add(new DefaultCellBinding<>(0, col, column.getHeader(), style, null));
        }
    }

    private void buildDataRows(List<SpreadsheetColumn<E>> columns,
                               List<E> entities,
                               List<CellBinding<E>> bindings,
                               int headerRows) {
        for (int row = 0; row < entities.size(); row++) {
            E entity = entities.get(row);
            int rowIndex = headerRows + row;
            for (int col = 0; col < columns.size(); col++) {
                SpreadsheetColumn<E> column = columns.get(col);
                Object rawValue = column.getValueProvider().apply(entity);
                Object value = formatValue(rawValue, column.getFormatter());
                bindings.add(new DefaultCellBinding<>(rowIndex, col, value, null, entity));
            }
        }
    }

    private Object formatValue(Object value, java.util.function.Function<Object, String> formatter) {
        if (formatter != null) {
            return formatter.apply(value);
        }
        return value == null ? "" : value;
    }

    private void buildRowGroups(SpreadsheetTableModel<E> model,
                                List<SpreadsheetColumn<E>> columns,
                                List<E> entities,
                                List<RowGroup> rowGroups,
                                int headerRows) {
        SpreadsheetTableModel.Grouping grouping = model.getGrouping();
        if (grouping == null || grouping.getColumnIds() == null || grouping.getColumnIds().isEmpty()) {
            return;
        }

        Map<String, Integer> columnIndex = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            columnIndex.put(columns.get(i).getId(), i);
        }

        List<String> groupColumns = grouping.getColumnIds();
        List<List<Object>> rowKeys = new ArrayList<>();
        for (E entity : entities) {
            List<Object> values = new ArrayList<>();
            for (String id : groupColumns) {
                Integer colIndex = columnIndex.get(id);
                if (colIndex == null) {
                    values.add(null);
                    continue;
                }
                SpreadsheetColumn<E> column = columns.get(colIndex);
                values.add(column.getValueProvider().apply(entity));
            }
            rowKeys.add(values);
        }

        boolean collapsed = !grouping.isExpandedByDefault();
        for (int level = 0; level < groupColumns.size(); level++) {
            int start = 0;
            List<Object> currentKey = prefixKey(rowKeys, 0, level);
            for (int i = 1; i < rowKeys.size(); i++) {
                List<Object> key = prefixKey(rowKeys, i, level);
                if (!Objects.equals(currentKey, key)) {
                    addGroupRange(rowGroups, start, i - 1, headerRows, collapsed);
                    start = i;
                    currentKey = key;
                }
            }
            addGroupRange(rowGroups, start, rowKeys.size() - 1, headerRows, collapsed);
        }

        rowGroups.sort(Comparator.comparingInt(RowGroup::getStartRow));
    }

    private List<Object> prefixKey(List<List<Object>> rowKeys, int index, int level) {
        if (rowKeys.isEmpty()) {
            return List.of();
        }
        List<Object> key = rowKeys.get(index);
        if (key.isEmpty()) {
            return List.of();
        }
        int end = Math.min(level + 1, key.size());
        return key.subList(0, end);
    }

    private void addGroupRange(List<RowGroup> rowGroups,
                               int start,
                               int end,
                               int headerRows,
                               boolean collapsed) {
        if (start < 0 || end < start) {
            return;
        }
        if (start == end) {
            return;
        }
        rowGroups.add(new RowGroup(headerRows + start, headerRows + end, collapsed, null));
    }
}
