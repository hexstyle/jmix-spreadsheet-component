package com.digtp.scm.portbalance.data;

import com.digtp.scm.entity.Country;
import com.digtp.scm.entity.Incoterms;
import com.digtp.scm.entity.Movement;
import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.Port;
import com.digtp.scm.entity.PreParty;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.Reason;
import com.digtp.scm.entity.SalesChannel;
import com.digtp.scm.entity.ShippingCombination;
import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.Tolerance;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TrackType;
import com.digtp.scm.entity.TransactionStatus;
import com.digtp.scm.entity.TransportCategory;
import com.digtp.scm.entity.TransportMode;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.Vessel;
import com.digtp.scm.entity.VesselLoad;
import com.digtp.scm.entity.VesselLoadItem;
import com.digtp.scm.entity.VesselLoadingReason;
import com.digtp.scm.entity.VesselLoadStatus;
import com.digtp.scm.entity.WarehouseTerminal;
import io.jmix.core.DataManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataFactory {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final UUID JAN_COUNTRY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID JAN_PORT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID JAN_TERMINAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID JAN_WAREHOUSE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID JAN_PLANT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID JAN_PRODUCT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID JAN_PRODUCT_PACKAGE_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID JAN_PRODUCT_B_ID = UUID.fromString("12121212-1212-1212-1212-121212121212");
    private static final UUID JAN_PRODUCT_PACKAGE_B_ID = UUID.fromString("13131313-1313-1313-1313-131313131313");
    private static final UUID JAN_TRANSPORT_CATEGORY_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID JAN_TRANSPORT_TYPE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID JAN_TRACK_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID JAN_TRACK_B_ID = UUID.fromString("abababab-abab-abab-abab-abababababab");
    private static final UUID JAN_COMBINATION_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID JAN_COMBINATION_B_ID = UUID.fromString("14141414-1414-1414-1414-141414141414");
    private static final UUID JAN_INCOTERMS_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID JAN_TOLERANCE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID JAN_PREPARTY_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID JAN_PREPARTY_B_ID = UUID.fromString("15151515-1515-1515-1515-151515151515");
    private static final UUID JAN_VESSEL_LOAD_ID = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
    private static final UUID JAN_VESSEL_LOAD_ITEM_ID = UUID.fromString("f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2");
    private static final UUID JAN_VESSEL_LOAD_NEG_ID = UUID.fromString("f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3");
    private static final UUID JAN_VESSEL_LOAD_ITEM_NEG_ID = UUID.fromString("f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4");
    private static final UUID JAN_VESSEL_LOAD_M1_ID = UUID.fromString("f5f5f5f5-f5f5-f5f5-f5f5-f5f5f5f5f5f5");
    private static final UUID JAN_VESSEL_LOAD_ITEM_M1_ID = UUID.fromString("f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6");
    private static final UUID JAN_VESSEL_LOAD_M2_ID = UUID.fromString("f7f7f7f7-f7f7-f7f7-f7f7-f7f7f7f7f7f7");
    private static final UUID JAN_VESSEL_LOAD_ITEM_M2_ID = UUID.fromString("f8f8f8f8-f8f8-f8f8-f8f8-f8f8f8f8f8f8");
    private static final UUID JAN_VESSEL_LOAD_M3_ID = UUID.fromString("f9f9f9f9-f9f9-f9f9-f9f9-f9f9f9f9f9f9");
    private static final UUID JAN_VESSEL_LOAD_ITEM_M3_ID = UUID.fromString("fafafafa-fafa-fafa-fafa-fafafafafafa");
    private static final UUID JAN_VESSEL_A_ID = UUID.fromString("16161616-1616-1616-1616-161616161616");
    private static final UUID JAN_VESSEL_B_ID = UUID.fromString("17171717-1717-1717-1717-171717171717");
    private static final UUID JAN_PLANT_SHIPMENT_REASON_1_ID = UUID.fromString("10101010-1010-1010-1010-101010101010");
    private static final UUID JAN_PLANT_SHIPMENT_REASON_2_ID = UUID.fromString("20202020-2020-2020-2020-202020202020");
    private static final UUID JAN_MOVEMENT_IN_1_ID = UUID.fromString("30303030-3030-3030-3030-303030303030");
    private static final UUID JAN_MOVEMENT_IN_2_ID = UUID.fromString("40404040-4040-4040-4040-404040404040");
    private static final UUID JAN_VESSEL_LOADING_REASON_ID = UUID.fromString("50505050-5050-5050-5050-505050505050");
    private static final UUID JAN_MOVEMENT_OUT_ID = UUID.fromString("60606060-6060-6060-6060-606060606060");
    private static final UUID JAN_VESSEL_LOADING_REASON_NEG_ID = UUID.fromString("70707070-7070-7070-7070-707070707070");
    private static final UUID JAN_MOVEMENT_OUT_NEG_ID = UUID.fromString("80808080-8080-8080-8080-808080808080");

    public static final int JAN_IN_VOLUME_1 = 1111;
    public static final int JAN_IN_VOLUME_2 = 2222;
    public static final int JAN_OUT_VOLUME = 3333;
    public static final int JAN_OUT_VOLUME_NEGATIVE = 500;
    public static final int JAN_MULTI_OUT_VOLUME_1 = 400;
    public static final int JAN_MULTI_OUT_VOLUME_2 = 600;
    public static final int JAN_MULTI_OUT_VOLUME_3 = 700;
    public static final int JAN_NEGATIVE_STOCK_VALUE = -500;

    private static final String JAN_COUNTRY_NAME = "PB26 Testland";
    private static final String JAN_COUNTRY_CODE = "QZ";
    private static final String JAN_PORT_CODE = "PB2601";
    private static final String JAN_PORT_NAME = "PB26 Port";
    private static final String JAN_TERMINAL_CODE = "PB26T1";
    private static final String JAN_TERMINAL_NAME = "PB26 Terminal";
    private static final String JAN_WAREHOUSE_NAME = "PB26 Warehouse";
    private static final String JAN_PLANT_CODE = "PB260";
    private static final String JAN_PLANT_NAME = "PB26 Plant";
    private static final String JAN_PRODUCT_NAME = "PB26 Product";
    private static final String JAN_PRODUCT_PACKAGE_CODE = "PB26PK";
    private static final String JAN_PRODUCT_PACKAGE_NAME = "PB26 Package";
    private static final String JAN_PRODUCT_B_NAME = "PB26 Product B";
    private static final String JAN_PRODUCT_PACKAGE_B_CODE = "PB26PKB";
    private static final String JAN_PRODUCT_PACKAGE_B_NAME = "PB26 Package B";
    private static final String JAN_TRANSPORT_CATEGORY_NAME = "PB26 Sea";
    private static final String JAN_TRANSPORT_TYPE_NAME = "PB26 Type";
    private static final String JAN_TRACK_NAME = "PB26 Track";
    private static final String JAN_TRACK_B_NAME = "PB26 Track Plan";
    private static final String JAN_INCOTERMS_NAME = "PB26 FOB";
    private static final String JAN_INCOTERMS_CODE = "FOB";
    private static final String JAN_TOLERANCE_NAME = "PB26 Tol";
    private static final String JAN_VESSEL_A_NAME = "PB26 Vessel A";
    private static final String JAN_VESSEL_B_NAME = "PB26 Vessel B";
    private static final String JAN_VESSEL_A_IMO = "1234567";
    private static final String JAN_VESSEL_B_IMO = "7654321";
    private final DataManager dataManager;

    public TestDataFactory(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public January2026PortBalanceData ensureJanuary2026PortBalanceData() {
        Country country = ensureCountry(JAN_COUNTRY_ID, JAN_COUNTRY_NAME, JAN_COUNTRY_CODE);
        Port port = ensurePort(JAN_PORT_ID, JAN_PORT_CODE, JAN_PORT_NAME, country);
        Terminal terminal = ensureTerminal(JAN_TERMINAL_ID, JAN_TERMINAL_CODE, JAN_TERMINAL_NAME, port);
        WarehouseTerminal warehouseTerminal = ensureWarehouseTerminal(JAN_WAREHOUSE_ID, JAN_WAREHOUSE_NAME, terminal);
        Plant plant = ensurePlant(JAN_PLANT_ID, JAN_PLANT_CODE, JAN_PLANT_NAME);
        Product product = ensureProduct(JAN_PRODUCT_ID, JAN_PRODUCT_NAME);
        ProductPackage productPackage = ensureProductPackage(
                JAN_PRODUCT_PACKAGE_ID, JAN_PRODUCT_PACKAGE_CODE, JAN_PRODUCT_PACKAGE_NAME);
        Product productB = ensureProduct(JAN_PRODUCT_B_ID, JAN_PRODUCT_B_NAME);
        ProductPackage productPackageB = ensureProductPackage(
                JAN_PRODUCT_PACKAGE_B_ID, JAN_PRODUCT_PACKAGE_B_CODE, JAN_PRODUCT_PACKAGE_B_NAME);
        TransportCategory category = ensureTransportCategory(
                JAN_TRANSPORT_CATEGORY_ID, JAN_TRANSPORT_CATEGORY_NAME, TransportMode.SEA);
        TransportType transportType = ensureTransportType(
                JAN_TRANSPORT_TYPE_ID, JAN_TRANSPORT_TYPE_NAME, category);
        Track track = ensureTrack(JAN_TRACK_ID, JAN_TRACK_NAME, TrackType.BASIC);
        Track trackPlan = ensureTrack(JAN_TRACK_B_ID, JAN_TRACK_B_NAME, TrackType.PLANNING);
        Vessel vesselA = ensureVessel(JAN_VESSEL_A_ID, JAN_VESSEL_A_NAME, JAN_VESSEL_A_IMO, transportType);
        Vessel vesselB = ensureVessel(JAN_VESSEL_B_ID, JAN_VESSEL_B_NAME, JAN_VESSEL_B_IMO, transportType);
        ShippingCombination combination = ensureShippingCombination(
                JAN_COMBINATION_ID, plant, warehouseTerminal, product, productPackage, transportType);
        ShippingCombination combinationB = ensureShippingCombination(
                JAN_COMBINATION_B_ID, plant, warehouseTerminal, productB, productPackageB, transportType);

        Incoterms incoterms = ensureIncoterms(JAN_INCOTERMS_ID, JAN_INCOTERMS_CODE, JAN_INCOTERMS_NAME);
        Tolerance tolerance = ensureTolerance(
                JAN_TOLERANCE_ID, JAN_TOLERANCE_NAME, BigDecimal.ONE, BigDecimal.TEN);

        PlantShipmentReason reason1 = ensurePlantShipmentReason(
                JAN_PLANT_SHIPMENT_REASON_1_ID,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                track,
                transportType,
                LocalDate.of(2026, 1, 2),
                JAN_IN_VOLUME_1,
                (short) 2
        );
        Movement inMovement1 = ensureMovement(
                JAN_MOVEMENT_IN_1_ID,
                reason1.getDate().plusDays(reason1.getLeadtimeOrDefault()),
                JAN_IN_VOLUME_1,
                track,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                reason1
        );

        PlantShipmentReason reason2 = ensurePlantShipmentReason(
                JAN_PLANT_SHIPMENT_REASON_2_ID,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                track,
                transportType,
                LocalDate.of(2026, 1, 5),
                JAN_IN_VOLUME_2,
                (short) 1
        );
        Movement inMovement2 = ensureMovement(
                JAN_MOVEMENT_IN_2_ID,
                reason2.getDate().plusDays(reason2.getLeadtimeOrDefault()),
                JAN_IN_VOLUME_2,
                track,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                reason2
        );

        PreParty preParty = ensurePreParty(
                JAN_PREPARTY_ID,
                plant,
                product,
                productPackage,
                terminal,
                port,
                incoterms,
                tolerance
        );
        PreParty prePartyB = ensurePreParty(
                JAN_PREPARTY_B_ID,
                plant,
                productB,
                productPackageB,
                terminal,
                port,
                incoterms,
                tolerance
        );
        VesselLoad vesselLoad = ensureVesselLoad(
                JAN_VESSEL_LOAD_ID,
                track,
                terminal,
                transportType,
                LocalDate.of(2026, 1, 6),
                LocalDate.of(2026, 1, 7),
                LocalDate.of(2026, 1, 6),
                LocalDate.of(2026, 1, 8)
        );
        VesselLoadItem vesselLoadItem = ensureVesselLoadItem(
                JAN_VESSEL_LOAD_ITEM_ID,
                vesselLoad,
                preParty,
                port,
                incoterms,
                tolerance,
                JAN_OUT_VOLUME
        );
        VesselLoadingReason outReason = ensureVesselLoadingReason(
                JAN_VESSEL_LOADING_REASON_ID,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                track,
                transportType,
                vesselLoadItem,
                LocalDate.of(2026, 1, 6),
                JAN_OUT_VOLUME
        );
        Movement outMovement = ensureMovement(
                JAN_MOVEMENT_OUT_ID,
                LocalDate.of(2026, 1, 6),
                JAN_OUT_VOLUME,
                track,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                outReason
        );

        VesselLoad negativeLoad = ensureVesselLoad(
                JAN_VESSEL_LOAD_NEG_ID,
                track,
                terminal,
                transportType,
                LocalDate.of(2026, 1, 7),
                LocalDate.of(2026, 1, 8),
                LocalDate.of(2026, 1, 7),
                LocalDate.of(2026, 1, 8)
        );
        VesselLoadItem negativeItem = ensureVesselLoadItem(
                JAN_VESSEL_LOAD_ITEM_NEG_ID,
                negativeLoad,
                preParty,
                port,
                incoterms,
                tolerance,
                JAN_OUT_VOLUME_NEGATIVE
        );
        VesselLoadingReason negativeReason = ensureVesselLoadingReason(
                JAN_VESSEL_LOADING_REASON_NEG_ID,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                track,
                transportType,
                negativeItem,
                LocalDate.of(2026, 1, 7),
                JAN_OUT_VOLUME_NEGATIVE
        );
        Movement negativeMovement = ensureMovement(
                JAN_MOVEMENT_OUT_NEG_ID,
                LocalDate.of(2026, 1, 7),
                JAN_OUT_VOLUME_NEGATIVE,
                track,
                plant,
                product,
                productPackage,
                warehouseTerminal,
                negativeReason
        );

        VesselLoad multiLoad1 = ensureVesselLoad(
                JAN_VESSEL_LOAD_M1_ID,
                track,
                terminal,
                transportType,
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 1, 11),
                vesselA,
                JAN_VESSEL_A_NAME,
                "TBN-0109A"
        );
        VesselLoadItem multiItem1 = ensureVesselLoadItem(
                JAN_VESSEL_LOAD_ITEM_M1_ID,
                multiLoad1,
                preParty,
                port,
                incoterms,
                tolerance,
                JAN_MULTI_OUT_VOLUME_1
        );

        VesselLoad multiLoad2 = ensureVesselLoad(
                JAN_VESSEL_LOAD_M2_ID,
                track,
                terminal,
                transportType,
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 1, 11),
                vesselB,
                JAN_VESSEL_B_NAME,
                "TBN-0109B"
        );
        VesselLoadItem multiItem2 = ensureVesselLoadItem(
                JAN_VESSEL_LOAD_ITEM_M2_ID,
                multiLoad2,
                preParty,
                port,
                incoterms,
                tolerance,
                JAN_MULTI_OUT_VOLUME_2
        );

        VesselLoad multiLoad3 = ensureVesselLoad(
                JAN_VESSEL_LOAD_M3_ID,
                trackPlan,
                terminal,
                transportType,
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 1, 11),
                vesselA,
                JAN_VESSEL_A_NAME,
                "TBN-0109C"
        );
        VesselLoadItem multiItem3 = ensureVesselLoadItem(
                JAN_VESSEL_LOAD_ITEM_M3_ID,
                multiLoad3,
                prePartyB,
                port,
                incoterms,
                tolerance,
                JAN_MULTI_OUT_VOLUME_3
        );

        return new January2026PortBalanceData(
                plant,
                product,
                productPackage,
                productB,
                productPackageB,
                terminal,
                warehouseTerminal,
                transportType,
                track,
                combination,
                combinationB,
                List.of(inMovement1, inMovement2),
                List.of(outMovement, negativeMovement),
                List.of(vesselLoad, negativeLoad, multiLoad1, multiLoad2, multiLoad3),
                List.of(vesselLoadItem, negativeItem, multiItem1, multiItem2, multiItem3)
        );
    }

    public Plant createPlant() {
        Plant plant = dataManager.create(Plant.class);
        plant.setCode(randomAlpha(5));
        plant.setName("Plant " + randomToken(6));
        return dataManager.save(plant);
    }

    public Product createProduct() {
        Product product = dataManager.create(Product.class);
        product.setName("Product " + randomToken(6));
        return dataManager.save(product);
    }

    public ProductPackage createProductPackage() {
        ProductPackage productPackage = dataManager.create(ProductPackage.class);
        productPackage.setCode("PKG-" + randomToken(6));
        productPackage.setName("Package " + randomToken(6));
        return dataManager.save(productPackage);
    }

    public Terminal createTerminal() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + randomToken(6));
        country.setAlpha2Code(randomAlpha(2));
        Country savedCountry = dataManager.save(country);

        Port port = dataManager.create(Port.class);
        port.setCode(randomAlpha(6));
        port.setName("Port " + randomToken(6));
        port.setCountry(savedCountry);
        Port savedPort = dataManager.save(port);

        Terminal terminal = dataManager.create(Terminal.class);
        terminal.setCode("TRM-" + randomToken(5));
        terminal.setName("Terminal " + randomToken(6));
        terminal.setPort(savedPort);
        return dataManager.save(terminal);
    }

    public WarehouseTerminal createWarehouseTerminal(Terminal terminal) {
        WarehouseTerminal warehouseTerminal = dataManager.create(WarehouseTerminal.class);
        warehouseTerminal.setName("Warehouse " + randomToken(6));
        warehouseTerminal.setTerminal(terminal);
        return dataManager.save(warehouseTerminal);
    }

    public TransportType createTransportType() {
        TransportCategory category = dataManager.create(TransportCategory.class);
        category.setName("Category " + randomToken(6));
        category.setTransportMode(TransportMode.SEA);
        TransportCategory savedCategory = dataManager.save(category);

        TransportType transportType = dataManager.create(TransportType.class);
        transportType.setName("Transport " + randomToken(6));
        transportType.setTransportCategory(savedCategory);
        return dataManager.save(transportType);
    }

    public Track createTrack(TrackType trackType) {
        Track track = dataManager.create(Track.class);
        track.setName("Track " + randomToken(6));
        track.setTrackType(trackType);
        return dataManager.save(track);
    }

    public ShippingCombination createShippingCombination(Plant plant,
                                                         WarehouseTerminal warehouseTerminal,
                                                         Product product,
                                                         ProductPackage productPackage,
                                                         TransportType transportType) {
        ShippingCombination combination = dataManager.create(ShippingCombination.class);
        combination.setPlant(plant);
        combination.setWarehouse(warehouseTerminal);
        combination.setProduct(product);
        combination.setProductPackage(productPackage);
        combination.setTransportType(transportType);
        return dataManager.save(combination);
    }

    public PlantShipmentReason createPlantShipmentReason(Plant plant,
                                                         Product product,
                                                         ProductPackage productPackage,
                                                         WarehouseTerminal warehouseTerminal,
                                                         Track track,
                                                         TransportType transportType,
                                                         LocalDate date,
                                                         int volume,
                                                         short leadtime) {
        PlantShipmentReason reason = dataManager.create(PlantShipmentReason.class);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setTransportType(transportType);
        reason.setDate(date);
        reason.setVolume(volume);
        reason.setLeadtime(leadtime);
        return dataManager.save(reason);
    }

    public Movement createMovementForIn(PlantShipmentReason reason) {
        Movement movement = dataManager.create(Movement.class);
        movement.setOriginPlant(reason.getOriginPlant());
        movement.setProduct(reason.getProduct());
        movement.setProductPackage(reason.getProductPackage());
        movement.setWarehouse(reason.getWarehouse());
        movement.setTrack(reason.getTrack());
        movement.setReason(reason);
        movement.setDate(reason.getDate().plusDays(reason.getLeadtimeOrDefault()));
        movement.setVolume(reason.getVolume());
        return dataManager.save(movement);
    }

    public VesselLoadingReason createVesselLoadingReason(Plant plant,
                                                         Product product,
                                                         ProductPackage productPackage,
                                                         WarehouseTerminal warehouseTerminal,
                                                         Track track,
                                                         TransportType transportType,
                                                         VesselLoadItem vesselLoadItem,
                                                         LocalDate date,
                                                         int volume) {
        VesselLoadingReason reason = dataManager.create(VesselLoadingReason.class);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setTransportType(transportType);
        reason.setVesselLoadItem(vesselLoadItem);
        reason.setDate(date);
        reason.setVolume(volume);
        return dataManager.save(reason);
    }

    public PreParty createPreParty(Terminal loadingTerminal) {
        Plant plant = createPlant();
        Product product = createProduct();
        ProductPackage productPackage = createProductPackage();

        PreParty preParty = dataManager.create(PreParty.class);
        preParty.setOriginPlant(plant);
        preParty.setProduct(product);
        preParty.setProductPackage(productPackage);
        preParty.setShipmentStartDate(LocalDate.of(2026, 4, 1));
        preParty.setShipmentEndDate(LocalDate.of(2026, 4, 2));
        preParty.setLoadingTerminal(loadingTerminal);
        preParty.setUnloadingPort(createPort());
        preParty.setIncoterms(createIncoterms());
        preParty.setTolerance(createTolerance());
        preParty.setSalesChannel(SalesChannel.RSO);
        preParty.setTransactionStatus(TransactionStatus.UNSOLD);
        preParty.setVolume(100);
        return dataManager.save(preParty);
    }

    public VesselLoad createVesselLoad(Track track,
                                       Terminal terminal,
                                       TransportType transportType,
                                       LocalDate loadingStart,
                                       LocalDate loadingEnd,
                                       LocalDate laycanStart,
                                       LocalDate laycanEnd) {
        VesselLoad vesselLoad = dataManager.create(VesselLoad.class);
        vesselLoad.setTrack(track);
        vesselLoad.setTerminal(terminal);
        vesselLoad.setTransportType(transportType);
        vesselLoad.setTbnNum("TBN-" + randomToken(4));
        vesselLoad.setVesselName("Vessel " + randomToken(4));
        vesselLoad.setPlanningLoadingStartDate(loadingStart);
        vesselLoad.setPlanningLoadingEndDate(loadingEnd);
        vesselLoad.setPlanningLaycanStartDate(laycanStart);
        vesselLoad.setPlanningLaycanEndDate(laycanEnd);
        vesselLoad.setVesselLoadStatus(VesselLoadStatus.DRAFT);
        return dataManager.save(vesselLoad);
    }

    public VesselLoadItem createVesselLoadItem(VesselLoad vesselLoad,
                                               PreParty preParty,
                                               int volume) {
        VesselLoadItem item = dataManager.create(VesselLoadItem.class);
        item.setVesselLoad(vesselLoad);
        item.setPreParty(preParty);
        item.setPort(preParty.getUnloadingPort());
        item.setIncoterms(preParty.getIncoterms());
        item.setClient("Client " + randomToken(4));
        item.setVolume(volume);
        item.setTolerance(preParty.getTolerance());
        return dataManager.save(item);
    }

    private Plant ensurePlant(UUID id, String code, String name) {
        Plant existing = dataManager.load(Plant.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Plant plant = dataManager.create(Plant.class);
        plant.setId(id);
        plant.setCode(code);
        plant.setName(name);
        return dataManager.save(plant);
    }

    private Product ensureProduct(UUID id, String name) {
        Product existing = dataManager.load(Product.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Product product = dataManager.create(Product.class);
        product.setId(id);
        product.setName(name);
        return dataManager.save(product);
    }

    private ProductPackage ensureProductPackage(UUID id, String code, String name) {
        ProductPackage existing = dataManager.load(ProductPackage.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        ProductPackage productPackage = dataManager.create(ProductPackage.class);
        productPackage.setId(id);
        productPackage.setCode(code);
        productPackage.setName(name);
        return dataManager.save(productPackage);
    }

    private Country ensureCountry(UUID id, String name, String alpha2Code) {
        Country existing = dataManager.load(Country.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Country country = dataManager.create(Country.class);
        country.setId(id);
        country.setName(name);
        country.setAlpha2Code(alpha2Code);
        return dataManager.save(country);
    }

    private Port ensurePort(UUID id, String code, String name, Country country) {
        Port existing = dataManager.load(Port.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Port port = dataManager.create(Port.class);
        port.setId(id);
        port.setCode(code);
        port.setName(name);
        port.setCountry(country);
        return dataManager.save(port);
    }

    private Terminal ensureTerminal(UUID id, String code, String name, Port port) {
        Terminal existing = dataManager.load(Terminal.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Terminal terminal = dataManager.create(Terminal.class);
        terminal.setId(id);
        terminal.setCode(code);
        terminal.setName(name);
        terminal.setPort(port);
        return dataManager.save(terminal);
    }

    private WarehouseTerminal ensureWarehouseTerminal(UUID id, String name, Terminal terminal) {
        WarehouseTerminal existing = dataManager.load(WarehouseTerminal.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        WarehouseTerminal warehouseTerminal = dataManager.create(WarehouseTerminal.class);
        warehouseTerminal.setId(id);
        warehouseTerminal.setName(name);
        warehouseTerminal.setTerminal(terminal);
        return dataManager.save(warehouseTerminal);
    }

    private TransportCategory ensureTransportCategory(UUID id, String name, TransportMode mode) {
        TransportCategory existing = dataManager.load(TransportCategory.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        TransportCategory category = dataManager.create(TransportCategory.class);
        category.setId(id);
        category.setName(name);
        category.setTransportMode(mode);
        return dataManager.save(category);
    }

    private TransportType ensureTransportType(UUID id, String name, TransportCategory category) {
        TransportType existing = dataManager.load(TransportType.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        TransportType transportType = dataManager.create(TransportType.class);
        transportType.setId(id);
        transportType.setName(name);
        transportType.setTransportCategory(category);
        return dataManager.save(transportType);
    }

    private Vessel ensureVessel(UUID id, String name, String imo, TransportType transportType) {
        Vessel existing = dataManager.load(Vessel.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Vessel vessel = dataManager.create(Vessel.class);
        vessel.setId(id);
        vessel.setName(name);
        vessel.setImo(imo);
        vessel.setTransportType(transportType);
        vessel.setIsArchived(false);
        return dataManager.save(vessel);
    }

    private Track ensureTrack(UUID id, String name, TrackType type) {
        Track existing = dataManager.load(Track.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Track track = dataManager.create(Track.class);
        track.setId(id);
        track.setName(name);
        track.setTrackType(type);
        return dataManager.save(track);
    }

    private ShippingCombination ensureShippingCombination(UUID id,
                                                          Plant plant,
                                                          WarehouseTerminal warehouseTerminal,
                                                          Product product,
                                                          ProductPackage productPackage,
                                                          TransportType transportType) {
        ShippingCombination existing = dataManager.load(ShippingCombination.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        ShippingCombination combination = dataManager.create(ShippingCombination.class);
        combination.setId(id);
        combination.setPlant(plant);
        combination.setWarehouse(warehouseTerminal);
        combination.setProduct(product);
        combination.setProductPackage(productPackage);
        combination.setTransportType(transportType);
        return dataManager.save(combination);
    }

    private Incoterms ensureIncoterms(UUID id, String code, String name) {
        Incoterms existing = dataManager.load(Incoterms.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Incoterms incoterms = dataManager.create(Incoterms.class);
        incoterms.setId(id);
        incoterms.setCode(code);
        incoterms.setName(name);
        return dataManager.save(incoterms);
    }

    private Tolerance ensureTolerance(UUID id, String name, BigDecimal min, BigDecimal max) {
        Tolerance existing = dataManager.load(Tolerance.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Tolerance tolerance = dataManager.create(Tolerance.class);
        tolerance.setId(id);
        tolerance.setName(name);
        tolerance.setMin(min);
        tolerance.setMax(max);
        return dataManager.save(tolerance);
    }

    private PreParty ensurePreParty(UUID id,
                                    Plant plant,
                                    Product product,
                                    ProductPackage productPackage,
                                    Terminal loadingTerminal,
                                    Port unloadingPort,
                                    Incoterms incoterms,
                                    Tolerance tolerance) {
        PreParty existing = dataManager.load(PreParty.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        PreParty preParty = dataManager.create(PreParty.class);
        preParty.setId(id);
        preParty.setOriginPlant(plant);
        preParty.setProduct(product);
        preParty.setProductPackage(productPackage);
        preParty.setShipmentStartDate(LocalDate.of(2026, 1, 1));
        preParty.setShipmentEndDate(LocalDate.of(2026, 1, 2));
        preParty.setLoadingTerminal(loadingTerminal);
        preParty.setUnloadingPort(unloadingPort);
        preParty.setIncoterms(incoterms);
        preParty.setTolerance(tolerance);
        preParty.setSalesChannel(SalesChannel.RSO);
        preParty.setTransactionStatus(TransactionStatus.UNSOLD);
        preParty.setVolume(JAN_OUT_VOLUME);
        preParty.setIsArchived(false);
        return dataManager.save(preParty);
    }

    private VesselLoad ensureVesselLoad(UUID id,
                                        Track track,
                                        Terminal terminal,
                                        TransportType transportType,
                                        LocalDate loadingStart,
                                        LocalDate loadingEnd,
                                        LocalDate laycanStart,
                                        LocalDate laycanEnd) {
        return ensureVesselLoad(
                id,
                track,
                terminal,
                transportType,
                loadingStart,
                loadingEnd,
                laycanStart,
                laycanEnd,
                null,
                "Vessel Jan",
                "TBN-0101"
        );
    }

    private VesselLoad ensureVesselLoad(UUID id,
                                        Track track,
                                        Terminal terminal,
                                        TransportType transportType,
                                        LocalDate loadingStart,
                                        LocalDate loadingEnd,
                                        LocalDate laycanStart,
                                        LocalDate laycanEnd,
                                        Vessel vessel,
                                        String vesselName,
                                        String tbnNum) {
        VesselLoad existing = dataManager.load(VesselLoad.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        VesselLoad vesselLoad = dataManager.create(VesselLoad.class);
        vesselLoad.setId(id);
        vesselLoad.setTrack(track);
        vesselLoad.setTerminal(terminal);
        vesselLoad.setTransportType(transportType);
        vesselLoad.setTbnNum(tbnNum);
        vesselLoad.setVesselName(vesselName);
        if (vessel != null) {
            vesselLoad.setVessel(vessel);
        }
        vesselLoad.setPlanningLoadingStartDate(loadingStart);
        vesselLoad.setPlanningLoadingEndDate(loadingEnd);
        vesselLoad.setPlanningLaycanStartDate(laycanStart);
        vesselLoad.setPlanningLaycanEndDate(laycanEnd);
        vesselLoad.setVesselLoadStatus(VesselLoadStatus.DRAFT);
        return dataManager.save(vesselLoad);
    }

    private VesselLoadItem ensureVesselLoadItem(UUID id,
                                                VesselLoad vesselLoad,
                                                PreParty preParty,
                                                Port port,
                                                Incoterms incoterms,
                                                Tolerance tolerance,
                                                int volume) {
        VesselLoadItem existing = dataManager.load(VesselLoadItem.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        VesselLoadItem item = dataManager.create(VesselLoadItem.class);
        item.setId(id);
        item.setVesselLoad(vesselLoad);
        item.setPreParty(preParty);
        item.setPort(port);
        item.setIncoterms(incoterms);
        item.setClient("Client Jan");
        item.setVolume(volume);
        item.setTolerance(tolerance);
        return dataManager.save(item);
    }

    private PlantShipmentReason ensurePlantShipmentReason(UUID id,
                                                          Plant plant,
                                                          Product product,
                                                          ProductPackage productPackage,
                                                          WarehouseTerminal warehouseTerminal,
                                                          Track track,
                                                          TransportType transportType,
                                                          LocalDate date,
                                                          int volume,
                                                          short leadtime) {
        PlantShipmentReason existing = dataManager.load(PlantShipmentReason.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        PlantShipmentReason reason = dataManager.create(PlantShipmentReason.class);
        reason.setId(id);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setTransportType(transportType);
        reason.setDate(date);
        reason.setVolume(volume);
        reason.setLeadtime(leadtime);
        return dataManager.save(reason);
    }

    private VesselLoadingReason ensureVesselLoadingReason(UUID id,
                                                          Plant plant,
                                                          Product product,
                                                          ProductPackage productPackage,
                                                          WarehouseTerminal warehouseTerminal,
                                                          Track track,
                                                          TransportType transportType,
                                                          VesselLoadItem vesselLoadItem,
                                                          LocalDate date,
                                                          int volume) {
        VesselLoadingReason existing = dataManager.load(VesselLoadingReason.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        VesselLoadingReason reason = dataManager.create(VesselLoadingReason.class);
        reason.setId(id);
        reason.setOriginPlant(plant);
        reason.setProduct(product);
        reason.setProductPackage(productPackage);
        reason.setWarehouse(warehouseTerminal);
        reason.setTrack(track);
        reason.setTransportType(transportType);
        reason.setVesselLoadItem(vesselLoadItem);
        reason.setDate(date);
        reason.setVolume(volume);
        return dataManager.save(reason);
    }

    private Movement ensureMovement(UUID id,
                                    LocalDate date,
                                    int volume,
                                    Track track,
                                    Plant plant,
                                    Product product,
                                    ProductPackage productPackage,
                                    WarehouseTerminal warehouseTerminal,
                                    Reason reason) {
        Movement existing = dataManager.load(Movement.class).id(id).optional().orElse(null);
        if (existing != null) {
            return existing;
        }
        Movement movement = dataManager.create(Movement.class);
        movement.setId(id);
        movement.setDate(date);
        movement.setVolume(volume);
        movement.setTrack(track);
        movement.setOriginPlant(plant);
        movement.setProduct(product);
        movement.setProductPackage(productPackage);
        movement.setWarehouse(warehouseTerminal);
        movement.setReason(reason);
        return dataManager.save(movement);
    }

    public record January2026PortBalanceData(Plant plant,
                                             Product product,
                                             ProductPackage productPackage,
                                             Product productB,
                                             ProductPackage productPackageB,
                                             Terminal terminal,
                                             WarehouseTerminal warehouseTerminal,
                                             TransportType transportType,
                                             Track track,
                                             ShippingCombination combination,
                                             ShippingCombination combinationB,
                                             List<Movement> inMovements,
                                             List<Movement> outMovements,
                                             List<VesselLoad> vesselLoads,
                                             List<VesselLoadItem> vesselLoadItems) {
        public int inVolume1() {
            return JAN_IN_VOLUME_1;
        }

        public int outVolume() {
            return JAN_OUT_VOLUME;
        }

        public int negativeStockValue() {
            return JAN_NEGATIVE_STOCK_VALUE;
        }

        public int multiVesselCount() {
            return 2;
        }
    }

    public List<VesselLoadItem> createMultiVesselDay(LocalDate loadingDate,
                                                     LocalDate laycanStart,
                                                     LocalDate laycanEnd,
                                                     Track track,
                                                     Terminal terminal,
                                                     TransportType transportType,
                                                     int vesselCount) {
        List<VesselLoadItem> items = new ArrayList<>();
        for (int i = 0; i < vesselCount; i++) {
            PreParty preParty = createPreParty(terminal);
            VesselLoad vesselLoad = createVesselLoad(
                    track,
                    terminal,
                    transportType,
                    loadingDate,
                    loadingDate.plusDays(1),
                    laycanStart,
                    laycanEnd
            );
            items.add(createVesselLoadItem(vesselLoad, preParty, 100 + i));
        }
        return items;
    }

    private Port createPort() {
        Country country = dataManager.create(Country.class);
        country.setName("Country " + randomToken(6));
        country.setAlpha2Code(randomAlpha(2));
        Country savedCountry = dataManager.save(country);

        Port port = dataManager.create(Port.class);
        port.setCode(randomAlpha(6));
        port.setName("Port " + randomToken(6));
        port.setCountry(savedCountry);
        return dataManager.save(port);
    }

    private Incoterms createIncoterms() {
        Incoterms incoterms = dataManager.create(Incoterms.class);
        incoterms.setName("Incoterms " + randomToken(6));
        incoterms.setCode(randomAlpha(3));
        return dataManager.save(incoterms);
    }

    private Tolerance createTolerance() {
        Tolerance tolerance = dataManager.create(Tolerance.class);
        tolerance.setName("Tolerance " + randomToken(6));
        tolerance.setMin(BigDecimal.ONE);
        tolerance.setMax(BigDecimal.TEN);
        return dataManager.save(tolerance);
    }

    private String randomToken(int length) {
        String value = java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return value.substring(0, length);
    }

    private String randomAlpha(int length) {
        StringBuilder builder = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}
