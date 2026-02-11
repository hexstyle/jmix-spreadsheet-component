package com.digtp.scm.portbalance.columns;

import com.digtp.scm.entity.Country;
import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.Port;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.ShippingCombination;
import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TrackType;
import com.digtp.scm.entity.TransportCategory;
import com.digtp.scm.entity.TransportMode;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.WarehouseTerminal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PortBalanceColumnKeyTest {

    @Test
    void comboKeyUsesTerminalFromWarehouseTerminal() {
        ShippingCombination combination = newShippingCombination();

        ComboKey key = ComboKey.from(combination);

        Assertions.assertThat(key.plant()).isSameAs(combination.getPlant());
        Assertions.assertThat(key.product()).isSameAs(combination.getProduct());
        Assertions.assertThat(key.productPackage()).isSameAs(combination.getProductPackage());
        Assertions.assertThat(key.transportType()).isSameAs(combination.getTransportType());
        Assertions.assertThat(key.terminal()).isSameAs(combination.getWarehouse().getTerminal());
    }

    @Test
    void trackKeyUsesNameAndType() {
        Track track = new Track();
        track.setName("AF Track");
        track.setTrackType(TrackType.BASIC);

        TrackKey key = TrackKey.from(track);

        Assertions.assertThat(key.name()).isEqualTo("AF Track");
        Assertions.assertThat(key.trackType()).isEqualTo(TrackType.BASIC);
    }

    @Test
    void columnKeyCombinesComboTrackAndMetric() {
        ShippingCombination combination = newShippingCombination();
        Track track = new Track();
        track.setName("Plan Track");
        track.setTrackType(TrackType.BASIC);

        PortBalanceColumnKey key = PortBalanceColumnKey.from(combination, track, PortBalanceMetric.STOCK);

        Assertions.assertThat(key.comboKey().plant()).isSameAs(combination.getPlant());
        Assertions.assertThat(key.trackKey().name()).isEqualTo("Plan Track");
        Assertions.assertThat(key.metric()).isEqualTo(PortBalanceMetric.STOCK);
    }

    private ShippingCombination newShippingCombination() {
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
}
