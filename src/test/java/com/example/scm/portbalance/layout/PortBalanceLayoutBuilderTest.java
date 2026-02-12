package com.example.scm.portbalance.layout;

import com.example.scm.entity.Country;
import com.example.scm.entity.Plant;
import com.example.scm.entity.Port;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.ShippingCombination;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.Track;
import com.example.scm.entity.TrackType;
import com.example.scm.entity.TransportCategory;
import com.example.scm.entity.TransportMode;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.WarehouseTerminal;
import com.example.scm.portbalance.aggregate.PortBalanceCell;
import com.example.scm.portbalance.aggregate.PortBalanceTable;
import com.example.scm.portbalance.columns.PortBalanceColumnKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
import com.example.scm.portbalance.rows.PortBalanceRow;
import com.hexstyle.jmixspreadsheet.layout.RowGroup;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class PortBalanceLayoutBuilderTest {

    private final PortBalanceLayoutBuilder builder = new PortBalanceLayoutBuilder();

    @Test
    void buildsThreeLevelColumnHeaders() {
        ShippingCombination combination = newCombination();
        Track track = newTrack("AF Track");

        List<PortBalanceColumnKey> columns = List.of(
                PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL),
                PortBalanceColumnKey.of(null, null, PortBalanceMetric.LAYCAN),
                PortBalanceColumnKey.of(null, null, PortBalanceMetric.TOTAL_OUT),
                PortBalanceColumnKey.from(combination, track, PortBalanceMetric.IN),
                PortBalanceColumnKey.from(combination, track, PortBalanceMetric.OUT)
        );
        List<PortBalanceRow> rows = List.of(PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1)));
        PortBalanceTable table = new PortBalanceTable(rows, columns, List.of());

        PortBalanceLayout layout = builder.buildLayout(table);

        String comboLabel = builder.formatComboLabel(columns.get(3).comboKey());
        String trackLabel = builder.formatTrackLabel(columns.get(3).trackKey());

        Assertions.assertThat(cellValue(layout, 0, 1)).isEqualTo("Vessel");
        Assertions.assertThat(cellValue(layout, 0, 2)).isEqualTo("Laycan");
        Assertions.assertThat(cellValue(layout, 0, 3)).isEqualTo("Total Out");
        Assertions.assertThat(cellValue(layout, 0, 4)).isEqualTo(comboLabel);
        Assertions.assertThat(cellValue(layout, 1, 4)).isEqualTo(trackLabel);
        Assertions.assertThat(cellValue(layout, 2, 4)).isEqualTo("IN");
        Assertions.assertThat(cellValue(layout, 2, 5)).isEqualTo("OUT");
    }

    @Test
    void buildsRowsWithMonthBreaks() {
        ShippingCombination combination = newCombination();
        Track track = newTrack("Plan");

        List<PortBalanceColumnKey> columns = List.of(
                PortBalanceColumnKey.from(combination, track, PortBalanceMetric.STOCK)
        );
        List<PortBalanceRow> rows = List.of(
                PortBalanceRow.monthBreak(LocalDate.of(2026, 2, 1), "2026-02"),
                PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1)),
                PortBalanceRow.dateRow(LocalDate.of(2026, 2, 2))
        );
        PortBalanceTable table = new PortBalanceTable(rows, columns, List.of());

        PortBalanceLayout layout = builder.buildLayout(table);

        int headerRows = 3;
        Assertions.assertThat(cellValue(layout, headerRows, 0)).isEqualTo("2026-02");
        Assertions.assertThat(cellValue(layout, headerRows + 1, 0)).isEqualTo("2026-02-01");
        Assertions.assertThat(cellValue(layout, headerRows + 2, 0)).isEqualTo("2026-02-02");
    }

    @Test
    void createsRowGroupForMultiVessel() {
        ShippingCombination combination = newCombination();
        Track track = newTrack("AF");

        PortBalanceColumnKey vesselKey = PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL);
        PortBalanceRow dateRow = PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1));
        PortBalanceRow detailRow1 = PortBalanceRow.vesselDetail(dateRow.getDate(), dateRow.getRowId(), 1, null, "Alpha");
        PortBalanceRow detailRow2 = PortBalanceRow.vesselDetail(dateRow.getDate(), dateRow.getRowId(), 2, null, "Beta");
        PortBalanceCell cell = new PortBalanceCell(detailRow1.getRowId(), vesselKey, "Alpha", null);

        PortBalanceTable table = new PortBalanceTable(
                List.of(dateRow, detailRow1, detailRow2),
                List.of(vesselKey),
                List.of(cell)
        );

        PortBalanceLayout layout = builder.buildLayout(table);

        Assertions.assertThat(layout.getRowGroups()).hasSize(1);
        RowGroup group = layout.getRowGroups().get(0);
        Assertions.assertThat(group.getStartRow()).isEqualTo(4);
        Assertions.assertThat(group.getEndRow()).isEqualTo(5);

        PortBalanceCellBinding binding = (PortBalanceCellBinding) cellBinding(layout, 4, 1);
        PortBalanceCellContext context = (PortBalanceCellContext) binding.getPivotContext();
        Assertions.assertThat(context.getDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        Assertions.assertThat(context.getMetric()).isEqualTo(PortBalanceMetric.VESSEL);
        Assertions.assertThat(context.getTerminal()).isNull();
        Assertions.assertThat(context.getTrackKey()).isNull();
        Assertions.assertThat(context.getVessel()).isEqualTo("Alpha");
        Assertions.assertThat(context.getVesselName()).isEqualTo("Alpha");
    }

    @Test
    void appliesStylesToHeaderAndDataCells() {
        ShippingCombination combination = newCombination();
        Track track = newTrack("AF");

        PortBalanceColumnKey inKey = PortBalanceColumnKey.from(combination, track, PortBalanceMetric.IN);
        LocalDate date = LocalDate.now().minusDays(1);
        PortBalanceRow row = PortBalanceRow.dateRow(date);
        PortBalanceCell cell = new PortBalanceCell("D:" + date, inKey, 10, null);

        PortBalanceTable table = new PortBalanceTable(
                List.of(row),
                List.of(
                        PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL),
                        PortBalanceColumnKey.of(null, null, PortBalanceMetric.LAYCAN),
                        PortBalanceColumnKey.of(null, null, PortBalanceMetric.TOTAL_OUT),
                        inKey
                ),
                List.of(cell)
        );

        PortBalanceLayout layout = builder.buildLayout(table);

        Assertions.assertThat(cellStyle(layout, 0, 1)).contains("#F3F4F6");
        Assertions.assertThat(cellStyle(layout, 3, 4)).contains("#2563EB");
    }

    @Test
    void closesTrackGroupsBetweenCombinationsWithSameTrack() {
        ShippingCombination comboA = newCombination();
        comboA.getPlant().setCode("PLT-A");
        comboA.getPlant().setName("Plant A");

        ShippingCombination comboB = newCombination();
        comboB.getPlant().setCode("PLT-B");
        comboB.getPlant().setName("Plant B");

        Track sharedTrack = newTrack("PB26 Track");

        List<PortBalanceColumnKey> columns = new ArrayList<>();
        columns.add(PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL));
        columns.add(PortBalanceColumnKey.of(null, null, PortBalanceMetric.LAYCAN));
        columns.add(PortBalanceColumnKey.of(null, null, PortBalanceMetric.TOTAL_OUT));
        columns.add(PortBalanceColumnKey.from(comboA, sharedTrack, PortBalanceMetric.IN));
        columns.add(PortBalanceColumnKey.from(comboA, sharedTrack, PortBalanceMetric.OUT));
        columns.add(PortBalanceColumnKey.from(comboA, sharedTrack, PortBalanceMetric.STOCK));
        columns.add(PortBalanceColumnKey.from(comboB, sharedTrack, PortBalanceMetric.IN));
        columns.add(PortBalanceColumnKey.from(comboB, sharedTrack, PortBalanceMetric.OUT));
        columns.add(PortBalanceColumnKey.from(comboB, sharedTrack, PortBalanceMetric.STOCK));

        PortBalanceTable table = new PortBalanceTable(
                List.of(PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1))),
                columns,
                List.of()
        );

        PortBalanceLayout layout = builder.buildLayout(table);

        String expectedTrackLabel = builder.formatTrackLabel(com.example.scm.portbalance.columns.TrackKey.from(sharedTrack));
        Assertions.assertThat(cellValue(layout, 1, 4)).isEqualTo(expectedTrackLabel);
        Assertions.assertThat(cellValue(layout, 1, 7)).isEqualTo(expectedTrackLabel);
        Assertions.assertThat(cellValue(layout, 2, 6)).isEqualTo("STOCK");
        Assertions.assertThat(cellValue(layout, 2, 9)).isEqualTo("STOCK");

        Assertions.assertThat(hasMergedRegion(layout, 1, 1, 4, 6)).isTrue();
        Assertions.assertThat(hasMergedRegion(layout, 1, 1, 7, 9)).isTrue();
    }

    private Object cellValue(PortBalanceLayout layout, int row, int column) {
        var binding = cellBinding(layout, row, column);
        return binding == null ? null : binding.getValue();
    }

    private String cellStyle(PortBalanceLayout layout, int row, int column) {
        var binding = cellBinding(layout, row, column);
        return binding == null ? null : binding.getStyle();
    }

    private com.hexstyle.jmixspreadsheet.layout.CellBinding<PortBalanceCell> cellBinding(PortBalanceLayout layout,
                                                                                        int row,
                                                                                        int column) {
        return layout.getCellBindings().stream()
                .filter(binding -> binding.getRowIndex() == row && binding.getColumnIndex() == column)
                .findFirst()
                .orElse(null);
    }

    private boolean hasMergedRegion(PortBalanceLayout layout,
                                    int firstRow,
                                    int lastRow,
                                    int firstColumn,
                                    int lastColumn) {
        return layout.getMergedRegions().stream()
                .anyMatch(region -> region.getFirstRow() == firstRow
                        && region.getLastRow() == lastRow
                        && region.getFirstColumn() == firstColumn
                        && region.getLastColumn() == lastColumn);
    }

    private ShippingCombination newCombination() {
        Plant plant = new Plant();
        plant.setCode("PLT01");
        plant.setName("Plant A");

        Product product = new Product();
        product.setName("Product A");

        ProductPackage productPackage = new ProductPackage();
        productPackage.setCode("PKG-A");
        productPackage.setName("Package A");

        TransportCategory category = new TransportCategory();
        category.setName("Sea");
        category.setTransportMode(TransportMode.SEA);

        TransportType transportType = new TransportType();
        transportType.setName("Sea Transport");
        transportType.setTransportCategory(category);

        Country country = new Country();
        country.setName("Country A");
        country.setAlpha2Code("AA");

        Port port = new Port();
        port.setCode("PORT1");
        port.setName("Port A");
        port.setCountry(country);

        Terminal terminal = new Terminal();
        terminal.setCode("TERM1");
        terminal.setName("Terminal A");
        terminal.setPort(port);

        WarehouseTerminal warehouseTerminal = new WarehouseTerminal();
        warehouseTerminal.setName("Warehouse A");
        warehouseTerminal.setTerminal(terminal);

        ShippingCombination combination = new ShippingCombination();
        combination.setPlant(plant);
        combination.setProduct(product);
        combination.setProductPackage(productPackage);
        combination.setTransportType(transportType);
        combination.setWarehouse(warehouseTerminal);

        return combination;
    }

    private Track newTrack(String name) {
        Track track = new Track();
        track.setName(name);
        track.setTrackType(TrackType.BASIC);
        return track;
    }
}
