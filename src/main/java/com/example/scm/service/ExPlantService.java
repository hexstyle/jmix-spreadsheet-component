package com.example.scm.service;

import com.example.scm.entity.Track;
import com.example.scm.spreadsheet.ProcessingResult;
import com.example.scm.spreadsheet.cell.InCell;
import com.example.scm.spreadsheet.cell.LeadtimeCell;
import com.example.scm.spreadsheet.cell.StockCell;
import com.example.scm.spreadsheet.cell.VolumeCell;
import com.example.scm.spreadsheet.combination.ExPlantCombination;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ExPlantService {
    
    private final PlantShipmentReasonService plantShipmentReasonService;
    private final LeadtimeService leadtimeService;
    private final MovementService movementService;
    private final StockService stockService;
    
    private Map<Class<?>, ExPlantProcessor<?>> processors;
    
    public ExPlantService(PlantShipmentReasonService plantShipmentReasonService,
                          LeadtimeService leadtimeService,
                          MovementService movementService,
                          StockService stockService) {
        this.plantShipmentReasonService = plantShipmentReasonService;
        this.leadtimeService = leadtimeService;
        this.movementService = movementService;
        this.stockService = stockService;
    }
    
    @PostConstruct
    void init() {
        processors = Map.of(
                VolumeCell.class, plantShipmentReasonService,
                LeadtimeCell.class, leadtimeService,
                InCell.class, movementService,
                StockCell.class, stockService);
    }
    
    @Transactional
    public <V> ProcessingResult<ExPlantCombination> process(ExPlantCombination combination, Class<?> cellType, V oldValue, V newValue) {
        @SuppressWarnings("unchecked")
        ExPlantProcessor<V> processor = (ExPlantProcessor<V>) processors.get(cellType);
        Objects.requireNonNull(processor, "processor cannot be null");
        return processor.process(combination, oldValue, newValue);
    }

    @Transactional
    public ProcessingResult<ExPlantCombination> transfer(List<ExPlantCombination> combinations, LocalDate date) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Transactional
    public ProcessingResult<ExPlantCombination> clone(List<ExPlantCombination> combinations, Track track) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Transactional
    public ProcessingResult<ExPlantCombination> delete(List<ExPlantCombination> combinations) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
