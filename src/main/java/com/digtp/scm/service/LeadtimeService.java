package com.digtp.scm.service;

import com.digtp.scm.entity.Leadtime;
import com.digtp.scm.entity.Movement;
import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.TerminalWarehouseReceiptReason;
import com.digtp.scm.repository.LeadtimeRepository;
import com.digtp.scm.repository.MovementRepository;
import com.digtp.scm.repository.PlantShipmentReasonRepository;
import com.digtp.scm.repository.TerminalWarehouseReceiptReasonRepository;
import com.digtp.scm.spreadsheet.ProcessingResult;
import com.digtp.scm.spreadsheet.cell.InCell;
import com.digtp.scm.spreadsheet.cell.LeadtimeCell;
import com.digtp.scm.spreadsheet.combination.ExPlantCombination;
import com.digtp.scm.spreadsheet.event.CellValueChangeEvent;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Stream;

@Service
public class LeadtimeService implements ExPlantProcessor<Short> {
    
    private final LeadtimeRepository leadtimeRepository;
    private final TerminalWarehouseReceiptReasonRepository terminalWarehouseReceiptReasonRepository;
    private final MovementRepository movementRepository;
    private final DataManager dataManager;
    private final StockService stockService;
    private final MovementService movementService;
    private final PlantShipmentReasonRepository plantShipmentReasonRepository;
    
    public LeadtimeService(LeadtimeRepository leadtimeRepository,
                           TerminalWarehouseReceiptReasonRepository terminalWarehouseReceiptReasonRepository,
                           MovementRepository movementRepository,
                           DataManager dataManager,
                           StockService stockService,
                           MovementService movementService,
                           PlantShipmentReasonRepository plantShipmentReasonRepository) {
        this.leadtimeRepository = leadtimeRepository;
        this.terminalWarehouseReceiptReasonRepository = terminalWarehouseReceiptReasonRepository;
        this.movementRepository = movementRepository;
        this.dataManager = dataManager;
        this.stockService = stockService;
        this.movementService = movementService;
        this.plantShipmentReasonRepository = plantShipmentReasonRepository;
    }
    
    @Override
    @Transactional
    public ProcessingResult<ExPlantCombination> process(ExPlantCombination combination, Short oldLeadtime, Short newLeadtime) {
        ProcessingResult<ExPlantCombination> result = new ProcessingResult<>();
        PlantShipmentReason plantShipmentReason = plantShipmentReasonRepository.findByCombination(
                        combination.getPlant(),
                        combination.getProduct(),
                        combination.getProductPackage(),
                        combination.getWarehouseTerminal(),
                        combination.getTransportType(),
                        combination.getTrack(),
                        combination.getDate())
                .orElse(null);
        if (plantShipmentReason == null) {
            throw new IllegalStateException("Unable to edit this cell");
        } else {
            if (newLeadtime == null) {
                newLeadtime = 0;
                result.add(new CellValueChangeEvent<>(LeadtimeCell.class, combination, newLeadtime));
            }
            TerminalWarehouseReceiptReason terminalWarehouseReceiptReason = terminalWarehouseReceiptReasonRepository.findByParentReason(plantShipmentReason)
                    .orElseThrow(() -> new IllegalStateException("TerminalWarehouseReceiptReason not found"));
            Movement movement = movementRepository.findByReason(plantShipmentReason)
                    .orElseThrow(() -> new IllegalStateException("Movement not found"));
            plantShipmentReason.setLeadtime(newLeadtime);
            terminalWarehouseReceiptReason.setDate(plantShipmentReason.getDate().plusDays(newLeadtime));
            movement.setDate(plantShipmentReason.getDate().plusDays(newLeadtime));
            dataManager.save(plantShipmentReason, terminalWarehouseReceiptReason, movement);
            
            ExPlantCombination stockCombination = combination.atDate(movement.getDate());
            stockService.findByCombination(stockCombination)
                    .orElseGet(() -> dataManager.save(stockService.create(stockCombination)));
            result.add(stockService.recalculateStocksSince(combination));
            
            Stream.of(oldLeadtime, newLeadtime)
                    .map(lt -> lt != null ? lt : 0)
                    .distinct()
                    .forEach(lt -> {
                        LocalDate inDate = plantShipmentReason.getDate().plusDays(lt);
                        ExPlantCombination inCombination = combination.atDate(inDate);
                        int inValue = movementService.calculateVolume(inCombination);
                        result.add(new CellValueChangeEvent<>(InCell.class, inCombination, inValue));
                    });
        }
        return result;
    }
    
    public Short findLeadtime(ExPlantCombination combination) {
        return leadtimeRepository.findByCombination(
                        combination.getPlant(),
                        combination.getProduct(),
                        combination.getProductPackage(),
                        combination.getWarehouseTerminal().getTerminal(),
                        combination.getTransportType(),
                        combination.getDate())
                .map(Leadtime::getDays)
                .orElse(null);
    }
}
