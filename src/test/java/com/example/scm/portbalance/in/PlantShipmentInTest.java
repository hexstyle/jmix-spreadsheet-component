package com.example.scm.portbalance.in;

import com.example.scm.JmixSpreadsheetApplication;
import com.example.scm.entity.Country;
import com.example.scm.entity.Plant;
import com.example.scm.entity.PlantShipmentReason;
import com.example.scm.entity.Port;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.Track;
import com.example.scm.entity.TrackType;
import com.example.scm.entity.TransportCategory;
import com.example.scm.entity.TransportMode;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.WarehouseTerminal;
import com.example.scm.service.MovementService;
import com.hexstyle.jmixspreadsheet.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
@ExtendWith(AuthenticatedAsAdmin.class)
class PlantShipmentInTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private MovementService movementService;

    @Test
    void movementIsCreatedFromPlantShipmentReason() {
        Plant plant = newPlant();
        Product product = newProduct();
        ProductPackage productPackage = newProductPackage();
        WarehouseTerminal warehouseTerminal = newWarehouseTerminal();
        TransportType transportType = newTransportType();
        Track track = newTrack();

        LocalDate shipmentDate = LocalDate.of(2026, 2, 1);
        short leadtime = 3;

        PlantShipmentReason reason = dataManager.create(PlantShipmentReason.class);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setDate(shipmentDate);
        reason.setVolume(120);
        reason.setLeadtime(leadtime);
        reason.setTransportType(transportType);

        var movement = movementService.create(reason);

        Assertions.assertThat(movement.getDate()).isEqualTo(shipmentDate.plusDays(leadtime));
        Assertions.assertThat(movement.getVolume()).isEqualTo(120);
        Assertions.assertThat(movement.getTrack()).isSameAs(track);
        Assertions.assertThat(movement.getOriginPlant()).isSameAs(plant);
        Assertions.assertThat(movement.getProduct()).isSameAs(product);
        Assertions.assertThat(movement.getProductPackage()).isSameAs(productPackage);
        Assertions.assertThat(movement.getWarehouse()).isSameAs(warehouseTerminal);
        Assertions.assertThat(movement.getReason()).isSameAs(reason);
    }

    private Plant newPlant() {
        Plant plant = dataManager.create(Plant.class);
        plant.setCode(uniqueToken(5));
        plant.setName("Plant " + uniqueToken(6));
        return plant;
    }

    private Product newProduct() {
        Product product = dataManager.create(Product.class);
        product.setName("Product " + uniqueToken(6));
        return product;
    }

    private ProductPackage newProductPackage() {
        ProductPackage productPackage = dataManager.create(ProductPackage.class);
        productPackage.setCode("PKG-" + uniqueToken(6));
        productPackage.setName("Package " + uniqueToken(6));
        return productPackage;
    }

    private WarehouseTerminal newWarehouseTerminal() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + uniqueToken(6));
        country.setAlpha2Code(uniqueToken(2));

        Port port = dataManager.create(Port.class);
        port.setCode(uniqueToken(6));
        port.setName("Port " + uniqueToken(6));
        port.setCountry(country);

        Terminal terminal = dataManager.create(Terminal.class);
        terminal.setCode(uniqueToken(5));
        terminal.setName("Terminal " + uniqueToken(6));
        terminal.setPort(port);

        WarehouseTerminal warehouseTerminal = dataManager.create(WarehouseTerminal.class);
        warehouseTerminal.setName("Warehouse " + uniqueToken(6));
        warehouseTerminal.setTerminal(terminal);
        return warehouseTerminal;
    }

    private TransportType newTransportType() {
        TransportCategory category = dataManager.create(TransportCategory.class);
        category.setName("Category " + uniqueToken(6));
        category.setTransportMode(TransportMode.SEA);

        TransportType transportType = dataManager.create(TransportType.class);
        transportType.setName("Transport " + uniqueToken(6));
        transportType.setTransportCategory(category);
        return transportType;
    }

    private Track newTrack() {
        Track track = dataManager.create(Track.class);
        track.setName("Track " + uniqueToken(6));
        track.setTrackType(TrackType.BASIC);
        return track;
    }

    private String uniqueToken(int length) {
        String value = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return value.substring(0, length);
    }
}
