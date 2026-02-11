package com.digtp.scm.service;

import com.digtp.scm.entity.Movement;
import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.Track;
import com.digtp.scm.repository.MovementRepository;
import com.digtp.scm.spreadsheet.ProcessingResult;
import com.digtp.scm.spreadsheet.combination.ExPlantCombination;
import com.digtp.scm.spreadsheet.combination.ExPlantGroup;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MovementService implements ExPlantProcessor<Integer> {
    
    private final MovementRepository movementRepository;
    private final FetchPlans fetchPlans;
    
    public MovementService(MovementRepository movementRepository,
                           FetchPlans fetchPlans) {
        this.movementRepository = movementRepository;
        this.fetchPlans = fetchPlans;
    }
    
    @Override
    public ProcessingResult<ExPlantCombination> process(ExPlantCombination combination, Integer oldValue, Integer newValue) {
        throw new UnsupportedOperationException("This cell is always readonly");
    }
    
    public Map<ExPlantCombination, Integer> calculateVolumes(Collection<ExPlantGroup> groups,
                                                             Collection<Track> tracks,
                                                             LocalDate periodFrom,
                                                             LocalDate periodTo) {
        Map<ExPlantCombination, Integer> volumes = new HashMap<>();
        for (ExPlantGroup group : groups) {
            Set<Movement> movements = movementRepository.findByCombinationAndDateBetween(
                    group.getPlant(),
                    group.getProduct(),
                    group.getProductPackage(),
                    group.getWarehouseTerminal(),
                    group.getTransportType(),
                    tracks,
                    periodFrom,
                    periodTo);
            for (Movement movement : movements) {
                ExPlantCombination combination = new ExPlantCombination(group, movement.getTrack(), movement.getDate());
                volumes.put(combination, volumes.computeIfAbsent(combination, combo -> 0) + movement.getVolume());
            }
        }
        return volumes;
    }
    
    public int calculateVolume(ExPlantCombination combination) {
        return movementRepository.findByCombination(
                        combination.getPlant(),
                        combination.getProduct(),
                        combination.getProductPackage(),
                        combination.getWarehouseTerminal(),
                        combination.getTransportType(),
                        combination.getTrack(),
                        combination.getDate(),
                        null)
                .stream()
                .mapToInt(Movement::getVolume)
                .sum();
    }
    
    public Movement create(PlantShipmentReason plantShipmentReason) {
        Movement movement = movementRepository.create();
        movement.setOriginPlant(plantShipmentReason.getOriginPlant());
        movement.setProduct(plantShipmentReason.getProduct());
        movement.setProductPackage(plantShipmentReason.getProductPackage());
        movement.setWarehouse(plantShipmentReason.getWarehouse());
        movement.setTrack(plantShipmentReason.getTrack());
        movement.setDate(plantShipmentReason.getDate().plusDays(plantShipmentReason.getLeadtimeOrDefault()));
        movement.setVolume(plantShipmentReason.getVolume());
        movement.setReason(plantShipmentReason);
        return movement;
    }
    
    public List<Movement> getMovementsByCombination(ExPlantCombination combination) {
        return movementRepository.findExPlantMovementsByCombination(
                combination.getPlant(),
                combination.getProduct(),
                combination.getProductPackage(),
                combination.getWarehouseTerminal(),
                combination.getTrack(),
                combination.getTransportType(),
                combination.getDate(),
                fetchPlans.builder(Movement.class).addFetchPlan(FetchPlan.BASE)
                        .add("reason", FetchPlan.BASE)
                        .build());
    }
}
