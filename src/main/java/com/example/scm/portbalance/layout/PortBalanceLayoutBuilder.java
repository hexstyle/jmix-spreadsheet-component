package com.example.scm.portbalance.layout;

import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.TransportType;
import com.example.scm.portbalance.aggregate.PortBalanceCell;
import com.example.scm.portbalance.aggregate.PortBalanceTable;
import com.example.scm.portbalance.columns.ComboKey;
import com.example.scm.portbalance.columns.PortBalanceColumnKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
import com.example.scm.portbalance.columns.TrackKey;
import com.example.scm.portbalance.rows.PortBalanceRow;
import com.example.scm.portbalance.style.CellContext;
import com.example.scm.portbalance.style.PortBalanceStyleEngine;
import com.example.scm.portbalance.style.StylePalette;
import com.hexstyle.jmixspreadsheet.api.SpreadsheetTableModel;
import com.hexstyle.jmixspreadsheet.layout.CellBinding;
import com.hexstyle.jmixspreadsheet.layout.LayoutEngine;
import com.hexstyle.jmixspreadsheet.layout.MergedRegion;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PortBalanceLayoutBuilder implements LayoutEngine<PortBalanceCell> {

    private static final int HEADER_ROWS = 3;
    private static final int ROW_HEADER_COLUMNS = 1;
    private static final String HEADER_STYLE = "background-color:#F3F4F6;color:#111827;font-weight:600;";
    private static final String ROW_HEADER_STYLE = "background-color:#F9FAFB;color:#374151;";
    private final PortBalanceStyleEngine styleEngine = new PortBalanceStyleEngine();
    private final StylePalette stylePalette = StylePalette.portBalance();

    public PortBalanceLayout buildLayout(PortBalanceTable table) {
        if (table == null) {
            throw new IllegalArgumentException("PortBalanceTable is required");
        }

        List<PortBalanceColumnKey> columns = table.getColumns();
        List<PortBalanceRow> rows = table.getRows();

        int columnCount = ROW_HEADER_COLUMNS + columns.size();
        int rowCount = HEADER_ROWS + rows.size();

        List<CellBinding<PortBalanceCell>> bindings = new ArrayList<>();
        List<MergedRegion> mergedRegions = new ArrayList<>();
        List<RowGroup> rowGroups = new ArrayList<>();

        Map<String, Map<String, PortBalanceCell>> cellsByRow = mapCells(table.getCells());

        LocalDate today = LocalDate.now();

        buildHeaderRows(columns, bindings, mergedRegions);
        buildRowHeaders(rows, bindings, today);
        buildDataCells(columns, rows, cellsByRow, bindings, rowGroups, today);

        return new PortBalanceLayout(rowCount, columnCount, bindings, mergedRegions, rowGroups);
    }

    @Override
    public SpreadsheetLayout<PortBalanceCell> buildLayout(SpreadsheetTableModel<PortBalanceCell> model,
                                                          Iterable<PortBalanceCell> entities) {
        throw new UnsupportedOperationException("Use buildLayout(PortBalanceTable)");
    }

    public String formatComboLabel(ComboKey comboKey) {
        Plant plant = comboKey.plant();
        Product product = comboKey.product();
        ProductPackage productPackage = comboKey.productPackage();
        TransportType transportType = comboKey.transportType();
        Terminal terminal = comboKey.terminal();

        String plantCode = plant == null ? "" : plant.getCode();
        String productName = product == null ? "" : product.getName();
        String packageCode = productPackage == null ? "" : productPackage.getCode();
        String transportName = transportType == null ? "" : transportType.getName();
        String terminalCode = terminal == null ? "" : terminal.getCode();

        return "%s %s %s %s %s".formatted(
                plantCode,
                productName,
                packageCode,
                transportName,
                terminalCode
        ).trim();
    }

    public String formatTrackLabel(TrackKey trackKey) {
        if (trackKey == null) {
            return "";
        }
        String type = trackKey.trackType() == null ? "" : trackKey.trackType().name();
        return "%s %s".formatted(trackKey.name(), type).trim();
    }

    private void buildHeaderRows(List<PortBalanceColumnKey> columns,
                                 List<CellBinding<PortBalanceCell>> bindings,
                                 List<MergedRegion> mergedRegions) {
        if (columns.isEmpty()) {
            return;
        }

        List<PortBalanceColumnKey> fixedColumns = columns.stream()
                .filter(this::isFixedColumn)
                .toList();
        List<PortBalanceColumnKey> dataColumns = columns.stream()
                .filter(key -> !isFixedColumn(key))
                .toList();

        bindings.add(new PortBalanceCellBinding(0, 0, "Date", HEADER_STYLE, null, null));
        mergedRegions.add(new PortBalanceMergedRegion(0, HEADER_ROWS - 1, 0, 0));

        for (int i = 0; i < fixedColumns.size(); i++) {
            PortBalanceColumnKey key = fixedColumns.get(i);
            int columnIndex = ROW_HEADER_COLUMNS + i;
            bindings.add(new PortBalanceCellBinding(
                    0,
                    columnIndex,
                    formatFixedLabel(key.metric()),
                    HEADER_STYLE,
                    null,
                    null
            ));
            mergedRegions.add(new PortBalanceMergedRegion(0, HEADER_ROWS - 1, columnIndex, columnIndex));
        }

        if (dataColumns.isEmpty()) {
            return;
        }

        String currentComboId = null;
        String currentTrackId = null;
        ComboKey currentCombo = null;
        TrackKey currentTrack = null;
        int dataStart = ROW_HEADER_COLUMNS + fixedColumns.size();
        int comboStart = dataStart;
        int trackStart = dataStart;

        for (int i = 0; i < dataColumns.size(); i++) {
            PortBalanceColumnKey key = dataColumns.get(i);
            int columnIndex = dataStart + i;

            String comboId = comboKeyId(key.comboKey());
            if (!Objects.equals(currentComboId, comboId)) {
                closeHeaderGroup(1, trackStart, columnIndex - 1, currentTrack, mergedRegions);
                closeHeaderGroup(0, comboStart, columnIndex - 1, currentCombo, mergedRegions);
                currentCombo = key.comboKey();
                currentComboId = comboId;
                comboStart = columnIndex;
                bindings.add(new PortBalanceCellBinding(
                        0,
                        columnIndex,
                        formatComboLabel(currentCombo),
                        HEADER_STYLE,
                        null,
                        null
                ));
                currentTrack = null;
                currentTrackId = null;
                trackStart = columnIndex;
            }

            String trackId = trackKeyId(key.trackKey());
            if (!Objects.equals(currentTrackId, trackId)) {
                closeHeaderGroup(1, trackStart, columnIndex - 1, currentTrack, mergedRegions);
                currentTrack = key.trackKey();
                currentTrackId = trackId;
                trackStart = columnIndex;
                bindings.add(new PortBalanceCellBinding(
                        1,
                        columnIndex,
                        formatTrackLabel(currentTrack),
                        HEADER_STYLE,
                        null,
                        null
                ));
            }

            bindings.add(new PortBalanceCellBinding(
                    2,
                    columnIndex,
                    key.metric().name(),
                    HEADER_STYLE,
                    null,
                    null
            ));
        }

        closeHeaderGroup(0, comboStart, dataStart + dataColumns.size() - 1, currentCombo, mergedRegions);
        closeHeaderGroup(1, trackStart, dataStart + dataColumns.size() - 1, currentTrack, mergedRegions);
    }

    private void closeHeaderGroup(int row,
                                  int start,
                                  int end,
                                  Object groupKey,
                                  List<MergedRegion> mergedRegions) {
        if (groupKey == null) {
            return;
        }
        if (end > start) {
            mergedRegions.add(new PortBalanceMergedRegion(row, row, start, end));
        }
    }

    private void buildRowHeaders(List<PortBalanceRow> rows,
                                 List<CellBinding<PortBalanceCell>> bindings,
                                 LocalDate today) {
        for (int i = 0; i < rows.size(); i++) {
            PortBalanceRow row = rows.get(i);
            int rowIndex = HEADER_ROWS + i;
            String label;
            if (row.isMonthBreak()) {
                label = row.getLabel();
            } else if (row.isVesselDetail()) {
                label = "";
            } else {
                label = row.getDate().toString();
            }

            String style = resolveRowHeaderStyle(row, label, today);
            bindings.add(new PortBalanceCellBinding(
                    rowIndex,
                    0,
                    label,
                    style,
                    null,
                    null
            ));
        }
    }

    private void buildDataCells(List<PortBalanceColumnKey> columns,
                                List<PortBalanceRow> rows,
                                Map<String, Map<String, PortBalanceCell>> cellsByRow,
                                List<CellBinding<PortBalanceCell>> bindings,
                                List<RowGroup> rowGroups,
                                LocalDate today) {
        buildRowGroups(rows, rowGroups);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            PortBalanceRow row = rows.get(rowIndex);
            String rowId = row.getRowId();
            Map<String, PortBalanceCell> rowCells = cellsByRow.getOrDefault(rowId, Map.of());

            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                PortBalanceColumnKey columnKey = columns.get(colIndex);
                PortBalanceCell cell = rowCells.get(columnKeyId(columnKey));
                Object value = cell == null ? null : cell.getValue();
                String vessel = columnKey.metric() == PortBalanceMetric.VESSEL && value instanceof String text
                        ? text
                        : null;
                java.util.UUID vesselId = row.getVesselId();
                String vesselName = row.getVesselName();

                ComboKey comboKey = columnKey.comboKey();
                PortBalanceCellContext context = new PortBalanceCellContext(
                        row.getDate(),
                        columnKey.trackKey(),
                        comboKey == null ? null : comboKey.terminal(),
                        comboKey == null ? null : comboKey.transportType(),
                        columnKey.metric(),
                        vessel,
                        comboKey,
                        row.isMonthBreak(),
                        row.isVesselDetail(),
                        vesselId,
                        vesselName
                );

                String style = resolveCellStyle(row, columnKey, value, today);
                int spreadsheetRow = HEADER_ROWS + rowIndex;
                int spreadsheetCol = ROW_HEADER_COLUMNS + colIndex;

                bindings.add(new PortBalanceCellBinding(
                        spreadsheetRow,
                        spreadsheetCol,
                        value,
                        style,
                        cell,
                        context
                ));
            }
        }

        rowGroups.sort(Comparator.comparingInt(RowGroup::getStartRow));
    }

    private String resolveRowHeaderStyle(PortBalanceRow row, Object value, LocalDate today) {
        CellContext context = new CellContext(row, null, value, today);
        String style = styleEngine.resolveStyle(context, stylePalette);
        if (row != null && row.isMonthBreak()) {
            return style;
        }
        if (ROW_HEADER_STYLE == null || ROW_HEADER_STYLE.isBlank()) {
            return style;
        }
        if (style == null || style.isBlank()) {
            return ROW_HEADER_STYLE;
        }
        return style + ROW_HEADER_STYLE;
    }

    private String resolveCellStyle(PortBalanceRow row,
                                    PortBalanceColumnKey columnKey,
                                    Object value,
                                    LocalDate today) {
        CellContext context = new CellContext(row, columnKey, value, today);
        return styleEngine.resolveStyle(context, stylePalette);
    }

    private Map<String, Map<String, PortBalanceCell>> mapCells(List<PortBalanceCell> cells) {
        Map<String, Map<String, PortBalanceCell>> result = new HashMap<>();
        for (PortBalanceCell cell : cells) {
            result.computeIfAbsent(cell.getRowId(), key -> new HashMap<>())
                    .put(columnKeyId(cell.getColumnKey()), cell);
        }
        return result;
    }

    private String comboKeyId(ComboKey key) {
        if (key == null) {
            return "";
        }
        return "%s|%s|%s|%s|%s".formatted(
                value(key.plant() == null ? null : key.plant().getCode()),
                value(key.product() == null ? null : key.product().getName()),
                value(key.productPackage() == null ? null : key.productPackage().getCode()),
                value(key.transportType() == null ? null : key.transportType().getName()),
                value(key.terminal() == null ? null : key.terminal().getCode())
        );
    }

    private String trackKeyId(TrackKey key) {
        if (key == null) {
            return "";
        }
        String type = key.trackType() == null ? "" : key.trackType().name();
        return "%s|%s".formatted(value(key.name()), type);
    }

    private String columnKeyId(PortBalanceColumnKey key) {
        if (key == null) {
            return "";
        }
        return "%s|%s|%s".formatted(
                comboKeyId(key.comboKey()),
                trackKeyId(key.trackKey()),
                key.metric() == null ? "" : key.metric().name()
        );
    }

    private void buildRowGroups(List<PortBalanceRow> rows, List<RowGroup> rowGroups) {
        Map<String, GroupRange> ranges = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            PortBalanceRow row = rows.get(i);
            if (!row.isVesselDetail()) {
                continue;
            }
            String groupId = row.getGroupId();
            if (groupId == null) {
                continue;
            }
            int spreadsheetRow = HEADER_ROWS + i;
            GroupRange range = ranges.computeIfAbsent(groupId, key -> new GroupRange(spreadsheetRow));
            range.update(spreadsheetRow);
        }
        for (GroupRange range : ranges.values()) {
            rowGroups.add(new RowGroup(range.startRow, range.endRow, false, null));
        }
    }

    private boolean isFixedColumn(PortBalanceColumnKey key) {
        if (key == null) {
            return false;
        }
        return key.comboKey() == null
                && (key.metric() == PortBalanceMetric.VESSEL
                || key.metric() == PortBalanceMetric.LAYCAN
                || key.metric() == PortBalanceMetric.TOTAL_OUT);
    }

    private String formatFixedLabel(PortBalanceMetric metric) {
        if (metric == null) {
            return "";
        }
        return switch (metric) {
            case TOTAL_OUT -> "Total Out";
            case LAYCAN -> "Laycan";
            case VESSEL -> "Vessel";
            default -> metric.name();
        };
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private static class GroupRange {
        private int startRow;
        private int endRow;

        private GroupRange(int row) {
            this.startRow = row;
            this.endRow = row;
        }

        private void update(int row) {
            if (row < startRow) {
                startRow = row;
            }
            if (row > endRow) {
                endRow = row;
            }
        }
    }
}
