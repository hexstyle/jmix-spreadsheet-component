package com.example.scm.portbalance.style;

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
import com.example.scm.portbalance.columns.PortBalanceColumnKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
import com.example.scm.portbalance.rows.PortBalanceRow;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

class PortBalanceStyleRulesTest {

    private final PortBalanceStyleEngine engine = new PortBalanceStyleEngine();

    @Test
    void appliesTodayRowStyle() {
        LocalDate today = LocalDate.of(2026, 2, 1);
        PortBalanceRow row = PortBalanceRow.dateRow(today);
        PortBalanceColumnKey key = columnKey(PortBalanceMetric.IN);

        CellContext context = new CellContext(row, key, 10, today);
        EnumSet<StyleToken> tokens = EnumSet.copyOf(engine.resolveTokens(context));

        Assertions.assertThat(tokens).contains(StyleToken.TODAY_ROW);
    }

    @Test
    void appliesMonthBreakStyle() {
        PortBalanceRow row = PortBalanceRow.monthBreak(LocalDate.of(2026, 2, 1), "2026-02");
        PortBalanceColumnKey key = columnKey(PortBalanceMetric.STOCK);

        CellContext context = new CellContext(row, key, 100, LocalDate.of(2026, 2, 5));
        EnumSet<StyleToken> tokens = EnumSet.copyOf(engine.resolveTokens(context));

        Assertions.assertThat(tokens).contains(StyleToken.MONTH_BREAK);
    }

    @Test
    void appliesNegativeStockStyle() {
        PortBalanceRow row = PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1));
        PortBalanceColumnKey key = columnKey(PortBalanceMetric.STOCK);

        CellContext context = new CellContext(row, key, -5, LocalDate.of(2026, 2, 3));
        EnumSet<StyleToken> tokens = EnumSet.copyOf(engine.resolveTokens(context));

        Assertions.assertThat(tokens).contains(StyleToken.NEGATIVE_STOCK);
    }

    @Test
    void appliesMetricStyles() {
        PortBalanceRow row = PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1));
        LocalDate today = LocalDate.of(2026, 2, 5);

        Assertions.assertThat(resolveTokens(row, PortBalanceMetric.IN, 1, today))
                .contains(StyleToken.METRIC_IN);
        Assertions.assertThat(resolveTokens(row, PortBalanceMetric.OUT, 1, today))
                .contains(StyleToken.METRIC_OUT);
        Assertions.assertThat(resolveTokens(row, PortBalanceMetric.STOCK, 1, today))
                .contains(StyleToken.METRIC_STOCK);
        Assertions.assertThat(resolveTokens(row, PortBalanceMetric.VESSEL, "Vessel A", today))
                .contains(StyleToken.METRIC_VESSEL);
        Assertions.assertThat(resolveTokens(row, PortBalanceMetric.LAYCAN, "2026-02-01 - 2026-02-03", today))
                .contains(StyleToken.METRIC_LAYCAN);
        Assertions.assertThat(resolveTokens(row, PortBalanceMetric.TOTAL_OUT, 10, today))
                .contains(StyleToken.METRIC_TOTAL_OUT);
    }

    @Test
    void appliesMultiVesselStyle() {
        PortBalanceRow row = PortBalanceRow.dateRow(LocalDate.of(2026, 2, 1));
        PortBalanceColumnKey key = columnKey(PortBalanceMetric.VESSEL);

        CellContext context = new CellContext(row, key, "2 vessels", LocalDate.of(2026, 2, 10));
        EnumSet<StyleToken> tokens = EnumSet.copyOf(engine.resolveTokens(context));

        Assertions.assertThat(tokens).contains(StyleToken.MULTI_VESSEL);
    }

    @Test
    void paletteUsesImageColors() {
        StylePalette palette = StylePalette.portBalance();

        Assertions.assertThat(palette.get(StyleToken.NEGATIVE_STOCK))
                .contains("#FEE2E2")
                .contains("#DC2626");
        Assertions.assertThat(palette.get(StyleToken.MULTI_VESSEL))
                .contains("#F3E8FF")
                .contains("#7C3AED");
        Assertions.assertThat(palette.get(StyleToken.TODAY_ROW))
                .contains("#2563EB");
        Assertions.assertThat(palette.get(StyleToken.MONTH_BREAK))
                .contains("#E5E7EB");
    }

    private EnumSet<StyleToken> resolveTokens(PortBalanceRow row,
                                              PortBalanceMetric metric,
                                              Object value,
                                              LocalDate today) {
        PortBalanceColumnKey key = columnKey(metric);
        CellContext context = new CellContext(row, key, value, today);
        return EnumSet.copyOf(engine.resolveTokens(context));
    }

    private PortBalanceColumnKey columnKey(PortBalanceMetric metric) {
        ShippingCombination combination = newCombination();
        Track track = newTrack();
        return PortBalanceColumnKey.from(combination, track, metric);
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

    private Track newTrack() {
        Track track = new Track();
        track.setName("Track A");
        track.setTrackType(TrackType.BASIC);
        return track;
    }
}
