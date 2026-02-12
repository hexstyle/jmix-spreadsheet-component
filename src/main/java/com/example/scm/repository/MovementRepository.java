package com.example.scm.repository;

import com.example.scm.entity.Movement;
import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Reason;
import com.example.scm.entity.Track;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.Warehouse;
import com.example.scm.entity.WarehouseTerminal;
import io.jmix.core.FetchPlan;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MovementRepository extends JmixDataRepository<Movement, UUID> {
    
    @Query("""
            select e
            from scm_Movement e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.warehouse = :warehouse
              and e.reason.dtype = 'PlantShipmentReason' and e.reason.transportType = :transportType
              and e.track = :track
              and e.date = :date""")
    Set<Movement> findByCombination(Plant plant,
                                    Product product,
                                    ProductPackage productPackage,
                                    Warehouse warehouse,
                                    TransportType transportType,
                                    Track track,
                                    LocalDate date,
                                    @Nullable FetchPlan fetchPlan);
    
    Optional<Movement> findByReason(Reason reason);
    
    @Query("""
            select e
            from scm_Movement e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.warehouse = :warehouse
              and e.reason.dtype = 'PlantShipmentReason' and e.reason.transportType = :transportType
              and e.track in :tracks
              and e.date >= :periodFrom
              and e.date <= :periodTo""")
    Set<Movement> findByCombinationAndDateBetween(Plant plant,
                                                  Product product,
                                                  ProductPackage productPackage,
                                                  Warehouse warehouse,
                                                  TransportType transportType,
                                                  Collection<Track> tracks,
                                                  LocalDate periodFrom,
                                                  LocalDate periodTo);

    @Query("""
            select e
            from scm_Movement e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.warehouse = :warehouse
              and e.reason.dtype = 'VesselLoadingReason' and e.reason.transportType = :transportType
              and e.track = :track
              and e.date >= :periodFrom
              and e.date <= :periodTo""")
    Set<Movement> findVesselLoadingByCombinationAndDateBetween(Plant plant,
                                                               Product product,
                                                               ProductPackage productPackage,
                                                               Warehouse warehouse,
                                                               TransportType transportType,
                                                               Track track,
                                                               LocalDate periodFrom,
                                                               LocalDate periodTo);
    
    @Query("""
            select m
            from scm_Movement m
            join scm_Reason r
              on m.reason.id = r.id
            where m.originPlant = :plant
              and m.product = :product
              and m.productPackage = :productPackage
              and m.warehouse = :warehouseTerminal
              and m.track = :track
              and r.dtype = 'PlantShipmentReason' and r.transportType = :transportType
              and m.date = :date
            order by r.date""")
    List<Movement> findExPlantMovementsByCombination(Plant plant,
                                                     Product product,
                                                     ProductPackage productPackage,
                                                     WarehouseTerminal warehouseTerminal,
                                                     Track track,
                                                     TransportType transportType,
                                                     LocalDate date,
                                                     FetchPlan fetchPlan);
}
