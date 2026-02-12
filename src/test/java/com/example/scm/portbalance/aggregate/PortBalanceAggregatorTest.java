package com.example.scm.portbalance.aggregate;

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
import com.example.scm.entity.ShippingCombination;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.Tolerance;
import com.example.scm.entity.Track;
import com.example.scm.entity.TrackType;
import com.example.scm.entity.TransportCategory;
import com.example.scm.entity.TransportMode;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.VesselLoad;
import com.example.scm.entity.VesselLoadItem;
import com.example.scm.entity.VesselLoadStatus;
import com.example.scm.entity.VesselLoadingReason;
import com.example.scm.entity.WarehouseTerminal;
import com.example.scm.portbalance.columns.PortBalanceColumnKey;
import com.example.scm.portbalance.columns.PortBalanceMetric;
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
import java.util.Objects;
import java.util.UUID;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
@ExtendWith(AuthenticatedAsAdmin.class)
@Transactional
@Rollback
class PortBalanceAggregatorTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PortBalanceAggregator aggregator;

    @Test
    void aggregatesPortBalanceTable() {
        Plant plant = savePlant();
        Product productA = saveProduct("Product A");
        Product productB = saveProduct("Product B");
        ProductPackage packageA = saveProductPackage("PKG-A");
        ProductPackage packageB = saveProductPackage("PKG-B");
        WarehouseTerminal warehouseTerminal = saveWarehouseTerminal();
        TransportType transportType = saveTransportType();
        Track track = saveTrack();

        ShippingCombination combination = new ShippingCombination();
        combination.setPlant(plant);
        combination.setProduct(productA);
        combination.setProductPackage(packageA);
        combination.setWarehouse(warehouseTerminal);
        combination.setTransportType(transportType);

        LocalDate from = LocalDate.of(2026, 1, 30);
        LocalDate to = LocalDate.of(2026, 2, 2);

        PlantShipmentReason inReason1 = createPlantShipmentReason(
                plant, productA, packageA, warehouseTerminal, transportType, track, from, 100, (short) 0);
        PlantShipmentReason inReason2 = createPlantShipmentReason(
                plant, productA, packageA, warehouseTerminal, transportType, track, LocalDate.of(2026, 2, 1), 50, (short) 0);
        VesselLoadingReason outReason = createVesselLoadingReason(
                plant, productA, packageA, warehouseTerminal, transportType, track, LocalDate.of(2026, 1, 31), 40);

        Movement inMovement1 = createMovement(inReason1, plant, productA, packageA, warehouseTerminal, track, from, 100);
        Movement inMovement2 = createMovement(inReason2, plant, productA, packageA, warehouseTerminal, track, LocalDate.of(2026, 2, 1), 50);
        Movement outMovement = createMovement(outReason, plant, productA, packageA, warehouseTerminal, track, LocalDate.of(2026, 1, 31), 40);

        saveAll(inReason1, inReason2, outReason, inMovement1, inMovement2, outMovement);

        createVesselLoadWithItems(track, warehouseTerminal.getTerminal(), transportType,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3),
                "Alpha",
                newItemSpec(plant, productA, packageA, 60),
                newItemSpec(plant, productB, packageB, 40));

        createVesselLoadWithItems(track, warehouseTerminal.getTerminal(), transportType,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 2),
                "Beta",
                newItemSpec(plant, productA, packageA, 10));

        PortBalanceTable table = aggregator.aggregate(List.of(combination), List.of(track), from, to);

        PortBalanceColumnKey inKey = PortBalanceColumnKey.from(combination, track, PortBalanceMetric.IN);
        PortBalanceColumnKey outKey = PortBalanceColumnKey.from(combination, track, PortBalanceMetric.OUT);
        PortBalanceColumnKey stockKey = PortBalanceColumnKey.from(combination, track, PortBalanceMetric.STOCK);
        PortBalanceColumnKey vesselKey = PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL);
        PortBalanceColumnKey laycanKey = PortBalanceColumnKey.of(null, null, PortBalanceMetric.LAYCAN);
        PortBalanceColumnKey totalOutKey = PortBalanceColumnKey.of(null, null, PortBalanceMetric.TOTAL_OUT);

        Assertions.assertThat(cellValue(table, "D:2026-01-30", inKey)).isEqualTo(100);
        Assertions.assertThat(cellValue(table, "D:2026-01-30", outKey)).isEqualTo(0);
        Assertions.assertThat(cellValue(table, "D:2026-01-30", stockKey)).isEqualTo(100);

        Assertions.assertThat(cellValue(table, "D:2026-01-31", inKey)).isEqualTo(0);
        Assertions.assertThat(cellValue(table, "D:2026-01-31", outKey)).isEqualTo(40);
        Assertions.assertThat(cellValue(table, "D:2026-01-31", stockKey)).isEqualTo(60);

        Assertions.assertThat(cellValue(table, "D:2026-02-01", inKey)).isEqualTo(50);
        Assertions.assertThat(cellValue(table, "D:2026-02-01", outKey)).isEqualTo(70);
        Assertions.assertThat(cellValue(table, "D:2026-02-01", stockKey)).isEqualTo(40);
        Assertions.assertThat(cellValue(table, "D:2026-02-01", vesselKey)).isEqualTo("2 vessels");
        Assertions.assertThat(cellValue(table, "D:2026-02-01", laycanKey)).isNull();
        Assertions.assertThat(cellValue(table, "D:2026-02-01", totalOutKey)).isEqualTo(110);

        List<String> detailRowIds = table.getRows().stream()
                .filter(row -> row.isVesselDetail())
                .filter(row -> LocalDate.of(2026, 2, 1).equals(row.getDate()))
                .map(com.example.scm.portbalance.rows.PortBalanceRow::getRowId)
                .toList();
        Assertions.assertThat(detailRowIds).hasSize(2);
        Assertions.assertThat(detailRowIds.stream()
                        .map(rowId -> cellValue(table, rowId, outKey))
                        .filter(Objects::nonNull)
                        .toList())
                .containsExactlyInAnyOrder(60, 10);
        Assertions.assertThat(detailRowIds.stream()
                        .map(rowId -> cellValue(table, rowId, vesselKey))
                        .filter(Objects::nonNull)
                        .toList())
                .containsExactlyInAnyOrder("Alpha", "Beta");

        Assertions.assertThat(cellValue(table, "M:2026-01", inKey)).isEqualTo(100);
        Assertions.assertThat(cellValue(table, "M:2026-01", outKey)).isEqualTo(40);
        Assertions.assertThat(cellValue(table, "M:2026-02", inKey)).isEqualTo(50);
        Assertions.assertThat(cellValue(table, "M:2026-02", outKey)).isEqualTo(70);
        Assertions.assertThat(cellValue(table, "M:2026-02", totalOutKey)).isEqualTo(110);
    }

    @Test
    void groupsSameVesselAcrossTracksIntoSingleDetailRow() {
        Plant plant = savePlant();
        Product product = saveProduct("Product A");
        ProductPackage productPackage = saveProductPackage("PKG-A");
        WarehouseTerminal warehouseTerminal = saveWarehouseTerminal();
        TransportType transportType = saveTransportType();
        Track trackA = saveTrack();
        Track trackB = saveTrack();

        ShippingCombination combination = new ShippingCombination();
        combination.setPlant(plant);
        combination.setProduct(product);
        combination.setProductPackage(productPackage);
        combination.setWarehouse(warehouseTerminal);
        combination.setTransportType(transportType);

        LocalDate date = LocalDate.of(2026, 2, 5);

        createVesselLoadWithItems(trackA, warehouseTerminal.getTerminal(), transportType,
                date, date.plusDays(2),
                "Shared",
                newItemSpec(plant, product, productPackage, 30));
        createVesselLoadWithItems(trackB, warehouseTerminal.getTerminal(), transportType,
                date, date.plusDays(1),
                "Shared",
                newItemSpec(plant, product, productPackage, 20));
        createVesselLoadWithItems(trackA, warehouseTerminal.getTerminal(), transportType,
                date, date.plusDays(1),
                "Other",
                newItemSpec(plant, product, productPackage, 10));

        PortBalanceTable table = aggregator.aggregate(List.of(combination), List.of(trackA, trackB), date, date);

        PortBalanceColumnKey vesselKey = PortBalanceColumnKey.of(null, null, PortBalanceMetric.VESSEL);
        PortBalanceColumnKey outAKey = PortBalanceColumnKey.from(combination, trackA, PortBalanceMetric.OUT);
        PortBalanceColumnKey outBKey = PortBalanceColumnKey.from(combination, trackB, PortBalanceMetric.OUT);

        Assertions.assertThat(cellValue(table, "D:" + date, vesselKey)).isEqualTo("2 vessels");

        String sharedRow = findRowIdByVessel(table, vesselKey, "Shared");
        String otherRow = findRowIdByVessel(table, vesselKey, "Other");

        Assertions.assertThat(sharedRow).isNotNull();
        Assertions.assertThat(otherRow).isNotNull();

        Assertions.assertThat(cellValue(table, sharedRow, outAKey)).isEqualTo(30);
        Assertions.assertThat(cellValue(table, sharedRow, outBKey)).isEqualTo(20);
        Assertions.assertThat(cellValue(table, otherRow, outAKey)).isEqualTo(10);
        Assertions.assertThat(cellValue(table, otherRow, outBKey)).isNull();
    }

    private Object cellValue(PortBalanceTable table, String rowId, PortBalanceColumnKey key) {
        return table.getCells().stream()
                .filter(cell -> rowId.equals(cell.getRowId()) && key.equals(cell.getColumnKey()))
                .findFirst()
                .map(PortBalanceCell::getValue)
                .orElse(null);
    }

    private String findRowIdByVessel(PortBalanceTable table, PortBalanceColumnKey vesselKey, String vesselName) {
        return table.getCells().stream()
                .filter(cell -> vesselKey.equals(cell.getColumnKey()))
                .filter(cell -> Objects.equals(cell.getValue(), vesselName))
                .map(PortBalanceCell::getRowId)
                .findFirst()
                .orElse(null);
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

    private void createVesselLoadWithItems(Track track,
                                           Terminal terminal,
                                           TransportType transportType,
                                           LocalDate laycanStart,
                                           LocalDate laycanEnd,
                                           String vesselName,
                                           VesselItemSpec... itemSpecs) {
        VesselLoad vesselLoad = dataManager.create(VesselLoad.class);
        vesselLoad.setTrack(track);
        vesselLoad.setTerminal(terminal);
        vesselLoad.setTransportType(transportType);
        vesselLoad.setTbnNum("TBN-" + uniqueToken(4));
        vesselLoad.setVesselName(vesselName);
        vesselLoad.setPlanningLoadingStartDate(laycanStart);
        vesselLoad.setPlanningLoadingEndDate(laycanEnd);
        vesselLoad.setPlanningLaycanStartDate(laycanStart);
        vesselLoad.setPlanningLaycanEndDate(laycanEnd);
        vesselLoad.setVesselLoadStatus(VesselLoadStatus.DRAFT);
        dataManager.save(vesselLoad);

        for (VesselItemSpec spec : itemSpecs) {
            PreParty preParty = createPreParty(spec.plant, spec.product, spec.productPackage, terminal);
            dataManager.save(preParty);

            Port port = savePort();
            Incoterms incoterms = saveIncoterms();
            Tolerance tolerance = saveTolerance();

            VesselLoadItem item = dataManager.create(VesselLoadItem.class);
            item.setVesselLoad(vesselLoad);
            item.setPreParty(preParty);
            item.setPort(port);
            item.setIncoterms(incoterms);
            item.setClient("Client " + uniqueToken(4));
            item.setVolume(spec.volume);
            item.setTolerance(tolerance);
            dataManager.save(item);
        }
    }

    private PreParty createPreParty(Plant plant,
                                    Product product,
                                    ProductPackage productPackage,
                                    Terminal terminal) {
        PreParty preParty = dataManager.create(PreParty.class);
        preParty.setOriginPlant(plant);
        preParty.setProduct(product);
        preParty.setProductPackage(productPackage);
        preParty.setShipmentStartDate(LocalDate.of(2026, 1, 1));
        preParty.setShipmentEndDate(LocalDate.of(2026, 1, 2));
        preParty.setLoadingTerminal(terminal);
        preParty.setUnloadingPort(savePort());
        preParty.setIncoterms(saveIncoterms());
        preParty.setTolerance(saveTolerance());
        preParty.setSalesChannel(com.example.scm.entity.SalesChannel.RSO);
        preParty.setTransactionStatus(com.example.scm.entity.TransactionStatus.UNSOLD);
        preParty.setVolume(100);
        return preParty;
    }

    private Plant savePlant() {
        Plant plant = dataManager.create(Plant.class);
        plant.setCode(uniqueToken(5));
        plant.setName("Plant " + uniqueToken(8));
        return dataManager.save(plant);
    }

    private Product saveProduct(String name) {
        Product product = dataManager.create(Product.class);
        product.setName(name + " " + uniqueToken(6));
        return dataManager.save(product);
    }

    private ProductPackage saveProductPackage(String code) {
        ProductPackage productPackage = dataManager.create(ProductPackage.class);
        productPackage.setCode(code + "-" + uniqueToken(4));
        productPackage.setName("Package " + uniqueToken(6));
        return dataManager.save(productPackage);
    }

    private WarehouseTerminal saveWarehouseTerminal() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + uniqueToken(6));
        country.setAlpha2Code(uniqueAlpha2Code());
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

    private Port savePort() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + uniqueToken(6));
        country.setAlpha2Code(uniqueAlpha2Code());
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
        incoterms.setCode(letterToken(3));
        return dataManager.save(incoterms);
    }

    private Tolerance saveTolerance() {
        Tolerance tolerance = dataManager.create(Tolerance.class);
        tolerance.setName("Tolerance " + uniqueToken(6));
        tolerance.setMin(java.math.BigDecimal.ONE);
        tolerance.setMax(java.math.BigDecimal.TEN);
        return dataManager.save(tolerance);
    }

    private VesselItemSpec newItemSpec(Plant plant,
                                       Product product,
                                       ProductPackage productPackage,
                                       int volume) {
        return new VesselItemSpec(plant, product, productPackage, volume);
    }

    private String uniqueToken(int length) {
        String value = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return value.substring(0, length);
    }

    private String letterToken(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * alphabet.length());
            builder.append(alphabet.charAt(index));
        }
        return builder.toString();
    }

    private String uniqueAlpha2Code() {
        String code;
        do {
            code = letterToken(2);
        } while (countryCodeExists(code));
        return code;
    }

    private boolean countryCodeExists(String code) {
        Long count = dataManager.loadValue(
                        "select count(c) from scm_Country c where c.alpha2Code = :code", Long.class)
                .parameter("code", code)
                .one();
        return count != null && count > 0;
    }

    private record VesselItemSpec(Plant plant,
                                  Product product,
                                  ProductPackage productPackage,
                                  int volume) {
    }
}
