package com.example.scm.service;

import com.example.scm.entity.Movement;
import com.example.scm.entity.PlantShipmentReason;
import com.example.scm.entity.Stock;
import com.example.scm.entity.Track;
import com.example.scm.repository.MovementRepository;
import com.example.scm.repository.PlantShipmentReasonRepository;
import com.example.scm.repository.StockRepository;
import com.example.scm.spreadsheet.ProcessingResult;
import com.example.scm.spreadsheet.cell.StockCell;
import com.example.scm.spreadsheet.combination.ExPlantCombination;
import com.example.scm.spreadsheet.combination.ExPlantGroup;
import com.example.scm.spreadsheet.event.CellValueChangeEvent;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class StockService implements ExPlantProcessor<Integer> {
    
    private final DataManager dataManager;
    private final MovementRepository movementRepository;
    private final StockRepository stockRepository;
    private final FetchPlans fetchPlans;
    private final PlantShipmentReasonRepository plantShipmentReasonRepository;
    
    public StockService(DataManager dataManager,
                        MovementRepository movementRepository,
                        StockRepository stockRepository,
                        FetchPlans fetchPlans,
                        PlantShipmentReasonRepository plantShipmentReasonRepository) {
        this.dataManager = dataManager;
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.fetchPlans = fetchPlans;
        this.plantShipmentReasonRepository = plantShipmentReasonRepository;
    }
    
    @Override
    public ProcessingResult<ExPlantCombination> process(ExPlantCombination stock, Integer oldValue, Integer volume) {
        throw new UnsupportedOperationException("This cell is always readonly");
    }
    
    public Stock create(PlantShipmentReason plantShipmentReason) {
        Stock stock = dataManager.create(Stock.class);
        stock.setOriginPlant(plantShipmentReason.getOriginPlant());
        stock.setProduct(plantShipmentReason.getProduct());
        stock.setProductPackage(plantShipmentReason.getProductPackage());
        stock.setWarehouse(plantShipmentReason.getWarehouse());
        stock.setTrack(plantShipmentReason.getTrack());
        stock.setDate(plantShipmentReason.getDate().plusDays(plantShipmentReason.getLeadtimeOrDefault()));
        stock.setVolume(0);
        return stock;
    }
    
    public Stock create(ExPlantCombination combination) {
        Stock stock = dataManager.create(Stock.class);
        stock.setOriginPlant(combination.getPlant());
        stock.setProduct(combination.getProduct());
        stock.setProductPackage(combination.getProductPackage());
        stock.setWarehouse(combination.getWarehouseTerminal());
        stock.setTrack(combination.getTrack());
        stock.setDate(combination.getDate());
        stock.setVolume(0);
        return stock;
    }
    
    @Transactional
    public ProcessingResult<ExPlantCombination> recalculateStocksSince(ExPlantCombination combination) {
        ProcessingResult<ExPlantCombination> result = new ProcessingResult<>();
        int previousStockVolume = stockRepository.findLastBeforeDateByCombination(
                        combination.getPlant(),
                        combination.getProduct(),
                        combination.getProductPackage(),
                        combination.getWarehouseTerminal(),
                        combination.getTrack(),
                        combination.getDate())
                .map(Stock::getVolume)
                .orElse(0);
        List<Stock> stocks = stockRepository.findByCombinationSince(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouseTerminal(),
                combination.getTrack(),
                combination.getDate());
        for (Stock stock : stocks) {
            FetchPlan movementFetchPlan = fetchPlans.builder(Movement.class, FetchPlan.BASE)
                    .add("reason", FetchPlan.BASE)
                    .build();
            int movementsVolumeSum = movementRepository.findByCombination(
                            combination.getPlant(),
                            combination.getProduct(),
                            combination.getProductPackage(),
                            combination.getWarehouseTerminal(),
                            combination.getTransportType(),
                            combination.getTrack(),
                            stock.getDate(),
                            movementFetchPlan)
                    .stream()
                    .peek(this::reloadReason)
                    .filter(Movement::isValid)
                    .mapToInt(Movement::getVolume)
                    .sum();
            int newStockVolume = previousStockVolume + movementsVolumeSum;
            stock.setVolume(newStockVolume);
            result.add(new CellValueChangeEvent<>(StockCell.class, combination.atDate(stock.getDate()), newStockVolume));
            previousStockVolume = newStockVolume;
        }
        dataManager.saveAll(stocks);
        return result;
    }
    
    private void reloadReason(Movement movement) {
        if (movement.getReason() instanceof PlantShipmentReason psr) {
            FetchPlan plantShipmentReasonFetchPlan = fetchPlans.builder(PlantShipmentReason.class, FetchPlan.BASE)
                    .add("parentReason", FetchPlan.BASE)
                    .build();
            psr = plantShipmentReasonRepository.getById(psr.getId(), plantShipmentReasonFetchPlan);
            movement.setReason(psr);
        }
    }
    
    public Map<ExPlantCombination, Integer> calculateVolumes(Collection<ExPlantGroup> groups,
                                                             Collection<Track> tracks,
                                                             LocalDate periodFrom,
                                                             LocalDate periodTo) {
        Map<ExPlantCombination, Integer> volumes = new HashMap<>();
        for (ExPlantGroup group : groups) {
            Set<Stock> stocks = stockRepository.findByCombinationAndDateBetween(
                    group.getPlant(),
                    group.getProduct(),
                    group.getProductPackage(),
                    group.getWarehouseTerminal(),
                    // group.getTransportType() - transportType is not present in Stock
                    tracks,
                    periodFrom,
                    periodTo);
            for (Stock stock : stocks) {
                ExPlantCombination combination = new ExPlantCombination(group, stock.getTrack(), stock.getDate());
                volumes.put(combination, stock.getVolume());
            }
        }
        return volumes;
    }
    
    public Optional<Stock> findByCombination(ExPlantCombination combination) {
        return stockRepository.findByCombination(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouseTerminal(),
                combination.getTrack(),
                combination.getDate());
    }
    
    public Optional<Stock> findLastBeforeDateByCombination(ExPlantCombination combination) {
        return stockRepository.findLastBeforeDateByCombination(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouseTerminal(),
                combination.getTrack(),
                combination.getDate());
    }
}
