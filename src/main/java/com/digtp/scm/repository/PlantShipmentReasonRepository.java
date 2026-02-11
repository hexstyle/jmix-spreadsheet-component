package com.digtp.scm.repository;

import com.digtp.scm.entity.Plant;
import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.Product;
import com.digtp.scm.entity.ProductPackage;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TransportType;
import com.digtp.scm.entity.Warehouse;
import com.digtp.scm.entity.WarehouseTerminal;
import com.digtp.scm.spreadsheet.ProductWithPackage;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantShipmentReasonRepository extends JmixDataRepository<PlantShipmentReason, UUID> {
    
    /**
     * Same query may be achieved with cortege-construction if possible
     * <blockquote><pre>
     *    @ Query("""
     *             select e
     *             from scm_PlantShipmentReason e
     *             where e.originPlant in :plants
     *               and (e.product, e.productPackage) in :productsWithPackages
     *               and e.warehouse in :warehouseTerminals
     *               and e.track in :tracks
     *               and e.date >= :dateFrom
     *               and e.date <= :dateTo
     *             """)
     * </pre></blockquote>
     */
    default List<PlantShipmentReason> findByCombinations(Collection<Plant> plants,
                                                         Collection<ProductWithPackage> productsWithPackages,
                                                         Collection<WarehouseTerminal> warehouseTerminals,
                                                         Collection<TransportType> transportTypes,
                                                         Collection<Track> tracks,
                                                         LocalDate dateFrom,
                                                         LocalDate dateTo) {
        LogicalCondition cortegeCondition = LogicalCondition.or();
        for (ProductWithPackage productWithPackage : productsWithPackages) {
            cortegeCondition.add(LogicalCondition.and(
                    PropertyCondition.equal("product", productWithPackage.getProduct()),
                    PropertyCondition.equal("productPackage", productWithPackage.getProductPackage())));
        }
        return getDataManager().load(PlantShipmentReason.class)
                .condition(
                        LogicalCondition.and(
                                PropertyCondition.inList("originPlant", plants),
                                cortegeCondition,
                                PropertyCondition.inList("warehouse", warehouseTerminals),
                                PropertyCondition.inList("transportType", transportTypes),
                                PropertyCondition.inList("track", tracks),
                                PropertyCondition.greaterOrEqual("date", dateFrom),
                                PropertyCondition.lessOrEqual("date", dateTo)))
                .list();
    }
    
    @Query("""
            select e
            from scm_PlantShipmentReason e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.transportType = :transportType
              and e.warehouse = :warehouse
              and e.track = :track
              and e.date = :date""")
    Optional<PlantShipmentReason> findByCombination(Plant plant,
                                                    Product product,
                                                    ProductPackage productPackage,
                                                    Warehouse warehouse,
                                                    TransportType transportType,
                                                    Track track,
                                                    LocalDate date);
}