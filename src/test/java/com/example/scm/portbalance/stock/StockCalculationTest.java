package com.example.scm.portbalance.stock;

import com.example.scm.JmixSpreadsheetApplication;
import com.example.scm.entity.Country;
import com.example.scm.entity.Movement;
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
import com.example.scm.entity.VesselLoadingReason;
import com.example.scm.entity.WarehouseTerminal;
import com.hexstyle.jmixspreadsheet.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
@ExtendWith(AuthenticatedAsAdmin.class)
@Transactional
@Rollback
class StockCalculationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PortBalanceStockCalculator stockCalculator;

    @Test
    void stockIsCalculatedAsPreviousPlusInMinusOut() {
        Plant plant = savePlant();
        Product product = saveProduct();
        ProductPackage productPackage = saveProductPackage();
        WarehouseTerminal warehouseTerminal = saveWarehouseTerminal();
        TransportType transportType = saveTransportType();
        Track track = saveTrack();

        LocalDate day1 = LocalDate.of(2026, 1, 10);
        LocalDate day2 = day1.plusDays(1);
        LocalDate day3 = day1.plusDays(2);

        PlantShipmentReason inReasonDay1 = createPlantShipmentReason(
                plant, product, productPackage, warehouseTerminal, transportType, track, day1, 100, (short) 0);
        PlantShipmentReason inReasonDay3 = createPlantShipmentReason(
                plant, product, productPackage, warehouseTerminal, transportType, track, day3, 20, (short) 0);
        VesselLoadingReason outReasonDay2 = createVesselLoadingReason(
                plant, product, productPackage, warehouseTerminal, transportType, track, day2, 40);

        Movement inMovementDay1 = createMovement(inReasonDay1, plant, product, productPackage, warehouseTerminal, track, day1, 100);
        Movement inMovementDay3 = createMovement(inReasonDay3, plant, product, productPackage, warehouseTerminal, track, day3, 20);
        Movement outMovementDay2 = createMovement(outReasonDay2, plant, product, productPackage, warehouseTerminal, track, day2, 40);

        saveAll(inReasonDay1, inReasonDay3, outReasonDay2, inMovementDay1, inMovementDay3, outMovementDay2);

        List<PortBalanceStockCalculator.StockDay> days = stockCalculator.calculate(
                plant,
                product,
                productPackage,
                warehouseTerminal,
                transportType,
                track,
                day1,
                day3
        );

        Assertions.assertThat(days).hasSize(3);
        Assertions.assertThat(days.get(0).getDate()).isEqualTo(day1);
        Assertions.assertThat(days.get(0).getInVolume()).isEqualTo(100);
        Assertions.assertThat(days.get(0).getOutVolume()).isEqualTo(0);
        Assertions.assertThat(days.get(0).getStockVolume()).isEqualTo(100);

        Assertions.assertThat(days.get(1).getDate()).isEqualTo(day2);
        Assertions.assertThat(days.get(1).getInVolume()).isEqualTo(0);
        Assertions.assertThat(days.get(1).getOutVolume()).isEqualTo(40);
        Assertions.assertThat(days.get(1).getStockVolume()).isEqualTo(60);

        Assertions.assertThat(days.get(2).getDate()).isEqualTo(day3);
        Assertions.assertThat(days.get(2).getInVolume()).isEqualTo(20);
        Assertions.assertThat(days.get(2).getOutVolume()).isEqualTo(0);
        Assertions.assertThat(days.get(2).getStockVolume()).isEqualTo(80);
    }

    private void saveAll(Object... entities) {
        for (Object entity : entities) {
            dataManager.save(entity);
        }
    }

    private PlantShipmentReason createPlantShipmentReason(Plant plant,
                                                         Product product,
                                                         ProductPackage productPackage,
                                                         WarehouseTerminal warehouseTerminal,
                                                         TransportType transportType,
                                                         Track track,
                                                         LocalDate date,
                                                         int volume,
                                                         short leadtime) {
        PlantShipmentReason reason = dataManager.create(PlantShipmentReason.class);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setDate(date);
        reason.setVolume(volume);
        reason.setLeadtime(leadtime);
        reason.setTransportType(transportType);
        return reason;
    }

    private VesselLoadingReason createVesselLoadingReason(Plant plant,
                                                         Product product,
                                                         ProductPackage productPackage,
                                                         WarehouseTerminal warehouseTerminal,
                                                         TransportType transportType,
                                                         Track track,
                                                         LocalDate date,
                                                         int volume) {
        VesselLoadingReason reason = dataManager.create(VesselLoadingReason.class);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setDate(date);
        reason.setVolume(volume);
        reason.setTransportType(transportType);
        return reason;
    }

    private Movement createMovement(Object reason,
                                    Plant plant,
                                    Product product,
                                    ProductPackage productPackage,
                                    WarehouseTerminal warehouseTerminal,
                                    Track track,
                                    LocalDate date,
                                    int volume) {
        Movement movement = dataManager.create(Movement.class);
        movement.setOriginPlant(plant);
        movement.setProduct(product);
        movement.setProductPackage(productPackage);
        movement.setWarehouse(warehouseTerminal);
        movement.setTrack(track);
        movement.setDate(date);
        movement.setVolume(volume);
        if (reason instanceof PlantShipmentReason psr) {
            movement.setReason(psr);
        } else if (reason instanceof VesselLoadingReason vlr) {
            movement.setReason(vlr);
        }
        return movement;
    }

    private Plant savePlant() {
        Plant plant = dataManager.create(Plant.class);
        plant.setCode(uniqueToken(5));
        plant.setName("Plant " + uniqueToken(8));
        return dataManager.save(plant);
    }

    private Product saveProduct() {
        Product product = dataManager.create(Product.class);
        product.setName("Product " + uniqueToken(8));
        return dataManager.save(product);
    }

    private ProductPackage saveProductPackage() {
        ProductPackage productPackage = dataManager.create(ProductPackage.class);
        productPackage.setCode("PKG-" + uniqueToken(8));
        productPackage.setName("Package " + uniqueToken(6));
        return dataManager.save(productPackage);
    }

    private WarehouseTerminal saveWarehouseTerminal() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + uniqueToken(6));
        country.setAlpha2Code(uniqueToken(2));
        Country savedCountry = dataManager.save(country);

        Port port = dataManager.create(Port.class);
        port.setCode(uniqueToken(6));
        port.setName("Port " + uniqueToken(6));
        port.setCountry(savedCountry);
        Port savedPort = dataManager.save(port);

        Terminal terminal = dataManager.create(Terminal.class);
        terminal.setCode(uniqueToken(5));
        terminal.setName("Terminal " + uniqueToken(6));
        terminal.setPort(savedPort);
        Terminal savedTerminal = dataManager.save(terminal);

        WarehouseTerminal warehouseTerminal = dataManager.create(WarehouseTerminal.class);
        warehouseTerminal.setName("Warehouse " + uniqueToken(6));
        warehouseTerminal.setTerminal(savedTerminal);
        return dataManager.save(warehouseTerminal);
    }

    private TransportType saveTransportType() {
        TransportCategory category = dataManager.create(TransportCategory.class);
        category.setName("Category " + uniqueToken(6));
        category.setTransportMode(TransportMode.SEA);
        TransportCategory savedCategory = dataManager.save(category);

        TransportType transportType = dataManager.create(TransportType.class);
        transportType.setName("Transport Type " + uniqueToken(6));
        transportType.setTransportCategory(savedCategory);
        return dataManager.save(transportType);
    }

    private Track saveTrack() {
        Track track = dataManager.create(Track.class);
        track.setName("Track " + uniqueToken(6));
        track.setTrackType(TrackType.BASIC);
        return dataManager.save(track);
    }

    private String uniqueToken(int length) {
        String value = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return value.substring(0, length);
    }
}
