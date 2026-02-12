package com.example.scm.portbalance.stock;

import com.example.scm.entity.Movement;
import com.example.scm.entity.Plant;
import com.example.scm.entity.Product;
import com.example.scm.entity.ProductPackage;
import com.example.scm.entity.Track;
import com.example.scm.entity.TransportType;
import com.example.scm.entity.WarehouseTerminal;
import com.example.scm.repository.MovementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PortBalanceStockCalculator {

    private final MovementRepository movementRepository;

    public PortBalanceStockCalculator(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    public List<StockDay> calculate(Plant plant,
                                    Product product,
                                    ProductPackage productPackage,
                                    WarehouseTerminal warehouseTerminal,
                                    TransportType transportType,
                                    Track track,
                                    LocalDate periodFrom,
                                    LocalDate periodTo) {
        if (periodFrom == null || periodTo == null) {
            throw new IllegalArgumentException("Period bounds must be provided");
        }

        Set<Movement> inMovements = movementRepository.findByCombinationAndDateBetween(
                plant,
                product,
                productPackage,
                warehouseTerminal,
                transportType,
                List.of(track),
                periodFrom,
                periodTo
        );

        Set<Movement> outMovements = movementRepository.findVesselLoadingByCombinationAndDateBetween(
                plant,
                product,
                productPackage,
                warehouseTerminal,
                transportType,
                track,
                periodFrom,
                periodTo
        );

        Map<LocalDate, Integer> inByDate = sumByDate(inMovements);
        Map<LocalDate, Integer> outByDate = sumByDate(outMovements);

        List<StockDay> result = new ArrayList<>();
        int stock = 0;
        LocalDate current = periodFrom;
        while (!current.isAfter(periodTo)) {
            int inVolume = inByDate.getOrDefault(current, 0);
            int outVolume = outByDate.getOrDefault(current, 0);
            stock = stock + inVolume - outVolume;
            result.add(new StockDay(current, inVolume, outVolume, stock));
            current = current.plusDays(1);
        }
        return result;
    }

    private Map<LocalDate, Integer> sumByDate(Set<Movement> movements) {
        Map<LocalDate, Integer> result = new HashMap<>();
        for (Movement movement : movements) {
            LocalDate date = movement.getDate();
            int volume = movement.getVolume() != null ? movement.getVolume() : 0;
            result.put(date, result.getOrDefault(date, 0) + volume);
        }
        return result;
    }

    public static class StockDay {
        private final LocalDate date;
        private final int inVolume;
        private final int outVolume;
        private final int stockVolume;

        public StockDay(LocalDate date, int inVolume, int outVolume, int stockVolume) {
            this.date = date;
            this.inVolume = inVolume;
            this.outVolume = outVolume;
            this.stockVolume = stockVolume;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getInVolume() {
            return inVolume;
        }

        public int getOutVolume() {
            return outVolume;
        }

        public int getStockVolume() {
            return stockVolume;
        }
    }
}
