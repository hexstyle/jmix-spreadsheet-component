package com.example.scm.repository;

import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Stock;
import com.example.scm.entity.Track;
import com.example.scm.entity.Warehouse;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface StockRepository extends JmixDataRepository<Stock, UUID> {
    
    @Query("""
            select e
            from scm_Stock e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.warehouse = :warehouse
              and e.track = :track
              and e.date = :date""")
    Optional<Stock> findByCombination(Plant plant,
                                      Product product,
                                      ProductPackage productPackage,
                                      Warehouse warehouse,
                                      Track track,
                                      LocalDate date);
    
    default Optional<Stock> findLastBeforeDateByCombination(Plant plant,
                                                            Product product,
                                                            ProductPackage productPackage,
                                                            Warehouse warehouse,
                                                            Track track,
                                                            LocalDate date) {
        return getDataManager().load(Stock.class)
                .query("""
                        select e
                        from scm_Stock e
                        where e.originPlant = :plant
                          and e.product = :product
                          and e.productPackage = :productPackage
                          and e.warehouse = :warehouse
                          and e.track = :track
                          and e.date < :date
                        order by e.date desc""")
                .parameter("plant", plant)
                .parameter("product", product)
                .parameter("productPackage", productPackage)
                .parameter("warehouse", warehouse)
                .parameter("track", track)
                .parameter("date", date)
                .maxResults(1)
                .optional();
    }
    
    @Query("""
            select e
            from scm_Stock e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.warehouse = :warehouse
              and e.track = :track
              and e.date >= :date
            order by e.date asc""")
    List<Stock> findByCombinationSince(Plant plant,
                                       Product product,
                                       ProductPackage productPackage,
                                       Warehouse warehouse,
                                       Track track,
                                       LocalDate date);
    
    @Query("""
            select e
            from scm_Stock e
            where e.originPlant = :plant
              and e.product = :product
              and e.productPackage = :productPackage
              and e.warehouse = :warehouse
              and e.track in :tracks
              and e.date >= :periodFrom
              and e.date <= :periodTo""")
    Set<Stock> findByCombinationAndDateBetween(Plant plant,
                                               Product product,
                                               ProductPackage productPackage,
                                               Warehouse warehouse,
                                               Collection<Track> tracks,
                                               LocalDate periodFrom,
                                               LocalDate periodTo);
}