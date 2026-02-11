package com.digtp.scm.portbalance.data;

import com.digtp.scm.JmixSpreadsheetApplication;
import com.digtp.scm.entity.Movement;
import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TrackType;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.VesselLoad;
import com.digtp.scm.entity.VesselLoadItem;
import com.digtp.scm.entity.WarehouseTerminal;
import com.hexstyle.jmixspreadsheet.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(classes = JmixSpreadsheetApplication.class)
@ExtendWith(AuthenticatedAsAdmin.class)
@Transactional
@Rollback
class TestDataFactoryTest {

    @Autowired
    private DataManager dataManager;

    private TestDataFactory factory;

    @BeforeEach
    void setup() {
        factory = new TestDataFactory(dataManager);
    }

    @Test
    void createsBaseEntities() {
        Plant plant = factory.createPlant();
        Product product = factory.createProduct();
        ProductPackage productPackage = factory.createProductPackage();
        Terminal terminal = factory.createTerminal();
        WarehouseTerminal warehouseTerminal = factory.createWarehouseTerminal(terminal);
        TransportType transportType = factory.createTransportType();

        Assertions.assertThat(plant.getId()).isNotNull();
        Assertions.assertThat(product.getId()).isNotNull();
        Assertions.assertThat(productPackage.getId()).isNotNull();
        Assertions.assertThat(terminal.getId()).isNotNull();
        Assertions.assertThat(warehouseTerminal.getId()).isNotNull();
        Assertions.assertThat(warehouseTerminal.getTerminal().getId()).isEqualTo(terminal.getId());
        Assertions.assertThat(transportType.getId()).isNotNull();
        Assertions.assertThat(transportType.getTransportCategory()).isNotNull();
    }

    @Test
    void createsTracksForEachType() {
        Track basic = factory.createTrack(TrackType.BASIC);
        Track planning = factory.createTrack(TrackType.PLANNING);
        Track draft = factory.createTrack(TrackType.DRAFT);

        Assertions.assertThat(basic.getTrackType()).isEqualTo(TrackType.BASIC);
        Assertions.assertThat(planning.getTrackType()).isEqualTo(TrackType.PLANNING);
        Assertions.assertThat(draft.getTrackType()).isEqualTo(TrackType.DRAFT);
    }

    @Test
    void createsPlantShipmentReasonAndMovement() {
        Plant plant = factory.createPlant();
        Product product = factory.createProduct();
        ProductPackage productPackage = factory.createProductPackage();
        Terminal terminal = factory.createTerminal();
        WarehouseTerminal warehouseTerminal = factory.createWarehouseTerminal(terminal);
        TransportType transportType = factory.createTransportType();
        Track track = factory.createTrack(TrackType.BASIC);

        LocalDate date = LocalDate.of(2026, 4, 1);
        PlantShipmentReason reason = factory.createPlantShipmentReason(
                plant,
                product,
                productPackage,
                warehouseTerminal,
                track,
                transportType,
                date,
                120,
                (short) 2
        );

        Assertions.assertThat(reason.getLeadtime()).isEqualTo((short) 2);
        Assertions.assertThat(reason.getDate()).isEqualTo(date);
        Assertions.assertThat(reason.getVolume()).isEqualTo(120);
        Assertions.assertThat(reason.getOriginPlant().getId()).isEqualTo(plant.getId());

        Movement movement = factory.createMovementForIn(reason);
        Assertions.assertThat(movement.getDate()).isEqualTo(date.plusDays(2));
        Assertions.assertThat(movement.getVolume()).isEqualTo(120);
        Assertions.assertThat(movement.getReason().getId()).isEqualTo(reason.getId());
        Assertions.assertThat(movement.getWarehouse().getId()).isEqualTo(warehouseTerminal.getId());
        Assertions.assertThat(movement.getTrack().getId()).isEqualTo(track.getId());
    }

    @Test
    void createsMultiVesselDayWithLaycanRange() {
        Track track = factory.createTrack(TrackType.BASIC);
        Terminal terminal = factory.createTerminal();
        TransportType transportType = factory.createTransportType();

        LocalDate loadingDate = LocalDate.of(2026, 4, 15);
        LocalDate laycanStart = LocalDate.of(2026, 4, 16);
        LocalDate laycanEnd = LocalDate.of(2026, 4, 20);

        List<VesselLoadItem> items = factory.createMultiVesselDay(
                loadingDate,
                laycanStart,
                laycanEnd,
                track,
                terminal,
                transportType,
                2
        );

        Assertions.assertThat(items).hasSize(2);

        Set<java.util.UUID> vesselLoadIds = items.stream()
                .map(item -> item.getVesselLoad().getId())
                .collect(Collectors.toSet());
        Assertions.assertThat(vesselLoadIds).hasSize(2);

        for (VesselLoadItem item : items) {
            VesselLoad vesselLoad = item.getVesselLoad();
            Assertions.assertThat(vesselLoad.getPlanningLoadingStartDate()).isEqualTo(loadingDate);
            Assertions.assertThat(vesselLoad.getPlanningLaycanStartDate()).isEqualTo(laycanStart);
            Assertions.assertThat(vesselLoad.getPlanningLaycanEndDate()).isEqualTo(laycanEnd);
            Assertions.assertThat(item.getPreParty()).isNotNull();
        }
    }
}
