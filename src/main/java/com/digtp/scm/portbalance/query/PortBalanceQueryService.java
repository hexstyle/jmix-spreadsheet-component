package com.digtp.scm.portbalance.query;

import com.digtp.scm.entity.Movement;
import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.Terminal;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.VesselLoad;
import com.digtp.scm.entity.VesselLoadItem;
import com.digtp.scm.entity.VesselLoadingReason;
import com.digtp.scm.entity.WarehouseTerminal;
import com.digtp.scm.repository.MovementRepository;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class PortBalanceQueryService {

    private final MovementRepository movementRepository;
    private final DataManager dataManager;

    public PortBalanceQueryService(MovementRepository movementRepository, DataManager dataManager) {
        this.movementRepository = movementRepository;
        this.dataManager = dataManager;
    }

    public Set<Movement> findPlantShipmentMovements(Plant plant,
                                                    Product product,
                                                    ProductPackage productPackage,
                                                    WarehouseTerminal warehouseTerminal,
                                                    TransportType transportType,
                                                    Collection<Track> tracks,
                                                    LocalDate periodFrom,
                                                    LocalDate periodTo) {
        return movementRepository.findByCombinationAndDateBetween(
                plant,
                product,
                productPackage,
                warehouseTerminal,
                transportType,
                tracks,
                periodFrom,
                periodTo
        );
    }

    public List<VesselLoadingOutRecord> findVesselLoadingOutRecords(Track track,
                                                                    WarehouseTerminal warehouseTerminal,
                                                                    Terminal terminal,
                                                                    TransportType transportType,
                                                                    LocalDate periodFrom,
                                                                    LocalDate periodTo) {
        List<VesselLoadingReason> reasons = dataManager.load(VesselLoadingReason.class)
                .query("""
                        select r
                        from scm_VesselLoadingReason r
                        join r.vesselLoadItem i
                        join i.vesselLoad v
                        where r.track = :track
                          and r.warehouse = :warehouse
                          and v.terminal = :terminal
                          and v.transportType = :transportType
                          and r.date >= :periodFrom
                          and r.date <= :periodTo
                """)
                .parameter("track", track)
                .parameter("warehouse", warehouseTerminal)
                .parameter("terminal", terminal)
                .parameter("transportType", transportType)
                .parameter("periodFrom", periodFrom)
                .parameter("periodTo", periodTo)
                .fetchPlan(fetchPlan -> fetchPlan
                        .add("track")
                        .add("warehouse")
                        .add("volume")
                        .add("date")
                        .add("originPlant")
                        .add("product")
                        .add("productPackage")
                        .add("transportType")
                        .add("vesselLoadItem", itemPlan -> itemPlan
                                .add("preParty", prePartyPlan -> prePartyPlan
                                        .add("originPlant")
                                        .add("product")
                                        .add("productPackage"))
                                .add("vesselLoad", vesselLoadPlan -> vesselLoadPlan
                                        .add("terminal")
                                        .add("transportType")
                                        .add("actualLoadingStartDate")
                                        .add("planningLoadingStartDate")
                                        .add("planningLaycanStartDate")
                                        .add("actualLaycanStartDate")
                                        .add("actualLaycanEndDate")
                                        .add("planningLaycanEndDate"))))
                .list();

        List<VesselLoadingOutRecord> records = new ArrayList<>();
        for (VesselLoadingReason reason : reasons) {
            VesselLoadItem item = reason.getVesselLoadItem();
            if (item == null) {
                throw new IllegalStateException("Vessel loading reason has no vessel load item");
            }
            VesselLoad vesselLoad = item.getVesselLoad();
            if (vesselLoad == null) {
                throw new IllegalStateException("Vessel load item has no vessel load");
            }
            records.add(new VesselLoadingOutRecord(
                    reason,
                    item,
                    vesselLoad,
                    resolveOutDate(vesselLoad, reason),
                    resolveLaycanStart(vesselLoad),
                    resolveLaycanEnd(vesselLoad)
            ));
        }
        return records;
    }

    public Movement mapVesselLoadingOut(VesselLoadingReason reason) {
        VesselLoadItem item = reason.getVesselLoadItem();
        if (item == null) {
            throw new IllegalArgumentException("Vessel loading reason has no vessel load item");
        }
        VesselLoad vesselLoad = item.getVesselLoad();
        if (vesselLoad == null) {
            throw new IllegalArgumentException("Vessel load item has no vessel load");
        }

        Movement movement = movementRepository.create();
        movement.setReason(reason);
        movement.setDate(resolveOutDate(vesselLoad, reason));
        movement.setVolume(resolveOutVolume(reason, item));
        movement.setTrack(vesselLoad.getTrack());
        movement.setOriginPlant(item.getPreParty().getOriginPlant());
        movement.setProduct(item.getPreParty().getProduct());
        movement.setProductPackage(item.getPreParty().getProductPackage());
        movement.setWarehouse(reason.getWarehouse());
        reason.setTransportType(vesselLoad.getTransportType());
        return movement;
    }

    private LocalDate resolveOutDate(VesselLoad vesselLoad, VesselLoadingReason reason) {
        if (vesselLoad.getActualLoadingStartDate() != null) {
            return vesselLoad.getActualLoadingStartDate();
        }
        if (vesselLoad.getPlanningLoadingStartDate() != null) {
            return vesselLoad.getPlanningLoadingStartDate();
        }
        if (vesselLoad.getPlanningLaycanStartDate() != null) {
            return vesselLoad.getPlanningLaycanStartDate();
        }
        return reason.getDate();
    }

    private LocalDate resolveLaycanStart(VesselLoad vesselLoad) {
        if (vesselLoad.getActualLaycanStartDate() != null) {
            return vesselLoad.getActualLaycanStartDate();
        }
        return vesselLoad.getPlanningLaycanStartDate();
    }

    private LocalDate resolveLaycanEnd(VesselLoad vesselLoad) {
        if (vesselLoad.getActualLaycanEndDate() != null) {
            return vesselLoad.getActualLaycanEndDate();
        }
        return vesselLoad.getPlanningLaycanEndDate();
    }

    private Integer resolveOutVolume(VesselLoadingReason reason, VesselLoadItem item) {
        return reason.getVolume() != null ? reason.getVolume() : item.getVolume();
    }
}
