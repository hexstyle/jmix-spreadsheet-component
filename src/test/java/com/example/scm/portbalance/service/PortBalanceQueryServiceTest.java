package com.example.scm.portbalance.service;

import com.example.scm.JmixSpreadsheetApplication;
import com.example.scm.entity.Country;
import com.example.scm.entity.Incoterms;
import com.example.scm.entity.Movement;
import com.example.scm.entity.Plant;
import com.example.scm.entity.PlantShipmentReason;
import com.example.scm.entity.Port;
import com.example.scm.entity.PreParty;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.Tolerance;
import com.example.scm.entity.Track;
import com.example.scm.entity.TrackType;
import com.example.scm.entity.TransportCategory;
import com.example.scm.entity.TransportMode;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import com.example.scm.entity.VesselLoadingReason;
import com.example.scm.entity.WarehouseTerminal;
import com.example.scm.portbalance.query.PortBalanceQueryService;
import com.example.scm.portbalance.query.VesselLoadingOutRecord;
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
import java.util.Set;
import java.util.UUID;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
@ExtendWith(AuthenticatedAsAdmin.class)
@Transactional
@Rollback
class PortBalanceQueryServiceTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PortBalanceQueryService queryService;

    @Test
    void loadsInMovementsByCombination() {
        Plant plant = savePlant();
        Product product = saveProduct();
        ProductPackage productPackage = saveProductPackage();
        WarehouseTerminal warehouseTerminal = saveWarehouseTerminal();
        TransportType transportType = saveTransportType();
        Track track = saveTrack();

        PlantShipmentReason reason = dataManager.create(PlantShipmentReason.class);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setTransportType(transportType);
        reason.setDate(LocalDate.of(2026, 4, 1));
        reason.setVolume(120);
        reason.setLeadtime((short) 0);
        dataManager.save(reason);

        Movement movement = dataManager.create(Movement.class);
        movement.setOriginPlant(plant);
        movement.setProduct(product);
        movement.setProductPackage(productPackage);
        movement.setWarehouse(warehouseTerminal);
        movement.setTrack(track);
        movement.setDate(LocalDate.of(2026, 4, 1));
        movement.setVolume(120);
        movement.setReason(reason);
        dataManager.save(movement);

        Set<Movement> movements = queryService.findPlantShipmentMovements(
                plant,
                product,
                productPackage,
                warehouseTerminal,
                transportType,
                List.of(track),
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 2)
        );

        Assertions.assertThat(movements).hasSize(1);
        Movement loaded = movements.iterator().next();
        Assertions.assertThat(loaded.getReason()).isInstanceOf(PlantShipmentReason.class);
        Assertions.assertThat(loaded.getDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        Assertions.assertThat(loaded.getVolume()).isEqualTo(120);
    }

    @Test
    void loadsVesselLoadingOutRecordsWithLaycan() {
        Track track = saveTrack();
        Terminal terminal = saveTerminal();
        TransportType transportType = saveTransportType();
        WarehouseTerminal warehouseTerminal = saveWarehouseTerminal(terminal);

        PreParty preParty = savePreParty(terminal);

        VesselLoad vesselLoad = dataManager.create(VesselLoad.class);
        vesselLoad.setTrack(track);
        vesselLoad.setTerminal(terminal);
        vesselLoad.setTransportType(transportType);
        vesselLoad.setTbnNum("TBN-" + uniqueToken(4));
        vesselLoad.setVesselName("Vessel " + uniqueToken(4));
        vesselLoad.setPlanningLoadingStartDate(LocalDate.of(2026, 4, 5));
        vesselLoad.setPlanningLoadingEndDate(LocalDate.of(2026, 4, 6));
        vesselLoad.setActualLoadingStartDate(LocalDate.of(2026, 4, 4));
        vesselLoad.setPlanningLaycanStartDate(LocalDate.of(2026, 4, 7));
        vesselLoad.setPlanningLaycanEndDate(LocalDate.of(2026, 4, 8));
        vesselLoad.setActualLaycanStartDate(LocalDate.of(2026, 4, 6));
        vesselLoad.setActualLaycanEndDate(LocalDate.of(2026, 4, 9));
        dataManager.save(vesselLoad);

        VesselLoadItem item = dataManager.create(VesselLoadItem.class);
        item.setVesselLoad(vesselLoad);
        item.setPreParty(preParty);
        item.setPort(savePort());
        item.setIncoterms(saveIncoterms());
        item.setClient("Client " + uniqueToken(4));
        item.setVolume(80);
        item.setTolerance(saveTolerance());
        dataManager.save(item);

        VesselLoadingReason reason = dataManager.create(VesselLoadingReason.class);
        reason.setVesselLoadItem(item);
        reason.setOriginPlant(preParty.getOriginPlant());
        reason.setProduct(preParty.getProduct());
        reason.setProductPackage(preParty.getProductPackage());
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setDate(LocalDate.of(2026, 4, 3));
        reason.setVolume(80);
        dataManager.save(reason);

        VesselLoadingReason otherReason = dataManager.create(VesselLoadingReason.class);
        otherReason.setVesselLoadItem(item);
        otherReason.setOriginPlant(preParty.getOriginPlant());
        otherReason.setProduct(preParty.getProduct());
        otherReason.setProductPackage(preParty.getProductPackage());
        otherReason.setWarehouse(warehouseTerminal);
        otherReason.setTrack(saveTrack());
        otherReason.setDate(LocalDate.of(2026, 4, 3));
        otherReason.setVolume(80);
        dataManager.save(otherReason);

        List<VesselLoadingOutRecord> records = queryService.findVesselLoadingOutRecords(
                track,
                warehouseTerminal,
                terminal,
                transportType,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 10)
        );

        Assertions.assertThat(records).hasSize(1);
        VesselLoadingOutRecord record = records.get(0);
        Assertions.assertThat(record.reason().getTrack().getId()).isEqualTo(track.getId());
        Assertions.assertThat(record.reason().getWarehouse().getId()).isEqualTo(warehouseTerminal.getId());
        Assertions.assertThat(record.reason().getVolume()).isEqualTo(80);
        Assertions.assertThat(record.reason().getDate()).isEqualTo(LocalDate.of(2026, 4, 3));
        Assertions.assertThat(record.item().getPreParty().getOriginPlant().getId())
                .isEqualTo(preParty.getOriginPlant().getId());
        Assertions.assertThat(record.item().getPreParty().getProduct().getId())
                .isEqualTo(preParty.getProduct().getId());
        Assertions.assertThat(record.item().getPreParty().getProductPackage().getId())
                .isEqualTo(preParty.getProductPackage().getId());
        Assertions.assertThat(record.vesselLoad().getTerminal().getId()).isEqualTo(terminal.getId());
        Assertions.assertThat(record.vesselLoad().getTransportType().getId()).isEqualTo(transportType.getId());
        Assertions.assertThat(record.loadingStartDate()).isEqualTo(LocalDate.of(2026, 4, 4));
        Assertions.assertThat(record.laycanStart()).isEqualTo(LocalDate.of(2026, 4, 6));
        Assertions.assertThat(record.laycanEnd()).isEqualTo(LocalDate.of(2026, 4, 9));
    }

    private Plant savePlant() {
        Plant plant = dataManager.create(Plant.class);
        plant.setCode(uniqueToken(5));
        plant.setName("Plant " + uniqueToken(6));
        return dataManager.save(plant);
    }

    private Product saveProduct() {
        Product product = dataManager.create(Product.class);
        product.setName("Product " + uniqueToken(6));
        return dataManager.save(product);
    }

    private ProductPackage saveProductPackage() {
        ProductPackage productPackage = dataManager.create(ProductPackage.class);
        productPackage.setCode("PKG-" + uniqueToken(6));
        productPackage.setName("Package " + uniqueToken(6));
        return dataManager.save(productPackage);
    }

    private WarehouseTerminal saveWarehouseTerminal() {
        return saveWarehouseTerminal(saveTerminal());
    }

    private WarehouseTerminal saveWarehouseTerminal(Terminal terminal) {
        WarehouseTerminal warehouseTerminal = dataManager.create(WarehouseTerminal.class);
        warehouseTerminal.setName("Warehouse " + uniqueToken(6));
        warehouseTerminal.setTerminal(terminal);
        return dataManager.save(warehouseTerminal);
    }

    private Track saveTrack() {
        Track track = dataManager.create(Track.class);
        track.setName("Track " + uniqueToken(6));
        track.setTrackType(TrackType.BASIC);
        return dataManager.save(track);
    }

    private Terminal saveTerminal() {
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
        return dataManager.save(terminal);
    }

    private TransportType saveTransportType() {
        TransportCategory category = dataManager.create(TransportCategory.class);
        category.setName("Category " + uniqueToken(6));
        category.setTransportMode(TransportMode.SEA);
        TransportCategory savedCategory = dataManager.save(category);

        TransportType transportType = dataManager.create(TransportType.class);
        transportType.setName("Transport " + uniqueToken(6));
        transportType.setTransportCategory(savedCategory);
        return dataManager.save(transportType);
    }

    private PreParty savePreParty(Terminal terminal) {
        Plant plant = savePlant();
        Product product = saveProduct();
        ProductPackage productPackage = saveProductPackage();

        PreParty preParty = dataManager.create(PreParty.class);
        preParty.setOriginPlant(plant);
        preParty.setProduct(product);
        preParty.setProductPackage(productPackage);
        preParty.setShipmentStartDate(LocalDate.of(2026, 4, 1));
        preParty.setShipmentEndDate(LocalDate.of(2026, 4, 2));
        preParty.setLoadingTerminal(terminal);
        preParty.setUnloadingPort(savePort());
        preParty.setIncoterms(saveIncoterms());
        preParty.setTolerance(saveTolerance());
        preParty.setSalesChannel(com.example.scm.entity.SalesChannel.RSO);
        preParty.setTransactionStatus(com.example.scm.entity.TransactionStatus.UNSOLD);
        preParty.setVolume(100);
        return dataManager.save(preParty);
    }

    private Port savePort() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + uniqueToken(6));
        country.setAlpha2Code(uniqueToken(2));
        Country savedCountry = dataManager.save(country);

        Port port = dataManager.create(Port.class);
        port.setCode(uniqueToken(6));
        port.setName("Port " + uniqueToken(6));
        port.setCountry(savedCountry);
        return dataManager.save(port);
    }

    private Incoterms saveIncoterms() {
        Incoterms incoterms = dataManager.create(Incoterms.class);
        incoterms.setName("Incoterms " + uniqueToken(6));
        incoterms.setCode(uniqueAlpha(3));
        return dataManager.save(incoterms);
    }

    private Tolerance saveTolerance() {
        Tolerance tolerance = dataManager.create(Tolerance.class);
        tolerance.setName("Tolerance " + uniqueToken(6));
        tolerance.setMin(java.math.BigDecimal.ONE);
        tolerance.setMax(java.math.BigDecimal.TEN);
        return dataManager.save(tolerance);
    }

    private String uniqueToken(int length) {
        String value = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return value.substring(0, length);
    }

    private String uniqueAlpha(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder(length);
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
