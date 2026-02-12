package com.example.scm.repository;

import com.example.scm.entity.Leadtime;
import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Terminal;
import com.example.scm.entity.TransportType;
import io.jmix.core.repository.JmixDataRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadtimeRepository extends JmixDataRepository<Leadtime, UUID> {
    
    default Optional<Leadtime> findByCombination(Plant plant,
                                                 Product product,
                                                 ProductPackage productPackage,
                                                 Terminal terminal,
                                                 TransportType transportType,
                                                 LocalDate date) {
        return getDataManager().load(Leadtime.class)
                .query("""
                        select e
                        from scm_Leadtime e
                        where e.plant = :plant
                          and (e.product = :product or e.product is null)
                          and (e.productPackage = :productPackage or e.productPackage is null)
                          and e.terminal = :terminal
                          and (e.transportType = :transportType or e.transportType is null)
                          and :date >= e.dateStart and :date <= e.dateEnd
                        order by e.product.id desc, e.productPackage.id desc, e.transportType.id desc""")
                .parameter("plant", plant)
                .parameter("product", product)
                .parameter("productPackage", productPackage)
                .parameter("terminal", terminal)
                .parameter("transportType", transportType)
                .parameter("date", date)
                .maxResults(1)
                .optional();
    }
}