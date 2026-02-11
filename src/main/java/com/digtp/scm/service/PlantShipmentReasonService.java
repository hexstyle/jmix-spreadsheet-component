package com.digtp.scm.service;

import com.digtp.scm.entity.Movement;
import com.digtp.scm.entity.PlantShipmentReason;
import com.digtp.scm.entity.TerminalWarehouseReceiptReason;
import com.digtp.scm.repository.MovementRepository;
import com.digtp.scm.repository.PlantShipmentReasonRepository;
import com.digtp.scm.repository.TerminalWarehouseReceiptReasonRepository;
import com.digtp.scm.spreadsheet.ProcessingResult;
import com.digtp.scm.spreadsheet.cell.InCell;
import com.digtp.scm.spreadsheet.cell.LeadtimeCell;
import com.digtp.scm.spreadsheet.cell.VolumeCell;
import com.digtp.scm.spreadsheet.combination.ExPlantCombination;
import com.digtp.scm.spreadsheet.event.CellReadOnlyChangeEvent;
import com.digtp.scm.spreadsheet.event.CellValueChangeEvent;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class PlantShipmentReasonService implements ExPlantProcessor<Integer> {
    
    private final LeadtimeService leadtimeService;
    private final TerminalWarehouseReceiptReasonService terminalWarehouseReceiptReasonService;
    private final Metadata metadata;
    private final MovementService movementService;
    private final StockService stockService;
    private final DataManager dataManager;
    private final MovementRepository movementRepository;
    private final TerminalWarehouseReceiptReasonRepository terminalWarehouseReceiptReasonRepository;
    private final PlantShipmentReasonRepository plantShipmentReasonRepository;
    
    public PlantShipmentReasonService(LeadtimeService leadtimeService,
                                      TerminalWarehouseReceiptReasonService terminalWarehouseReceiptReasonService,
                                      Metadata metadata,
                                      MovementService movementService,
                                      StockService stockService,
                                      DataManager dataManager,
                                      MovementRepository movementRepository,
                                      TerminalWarehouseReceiptReasonRepository terminalWarehouseReceiptReasonRepository,
                                      PlantShipmentReasonRepository plantShipmentReasonRepository) {
        this.leadtimeService = leadtimeService;
        this.terminalWarehouseReceiptReasonService = terminalWarehouseReceiptReasonService;
        this.metadata = metadata;
        this.movementService = movementService;
        this.stockService = stockService;
        this.dataManager = dataManager;
        this.movementRepository = movementRepository;
        this.terminalWarehouseReceiptReasonRepository = terminalWarehouseReceiptReasonRepository;
        this.plantShipmentReasonRepository = plantShipmentReasonRepository;
    }
    
    @Override
    @Transactional
    public ProcessingResult<ExPlantCombination> process(ExPlantCombination combination, Integer oldVolume, Integer newVolume) {
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
            if (newVolume != null && newVolume != 0) {
                PlantShipmentReason plantShipmentReasonNew = create(combination);
                Short leadtime = leadtimeService.findLeadtime(combination);
                plantShipmentReasonNew.setLeadtime(leadtime);
                plantShipmentReasonNew.setVolume(newVolume);
                TerminalWarehouseReceiptReason terminalWarehouseReceiptReason = terminalWarehouseReceiptReasonService.create(plantShipmentReasonNew);
                Movement movement = movementService.create(plantShipmentReasonNew);
                
                ExPlantCombination stockCombination = combination.atDate(combination.getDate().plusDays(plantShipmentReasonNew.getLeadtimeOrDefault()));
                stockService.findByCombination(stockCombination)
                        .orElseGet(() -> dataManager.save(stockService.create(stockCombination)));
                dataManager.save(plantShipmentReasonNew, terminalWarehouseReceiptReason, movement);
                if (plantShipmentReasonNew.isValid()) {
                    result.add(stockService.recalculateStocksSince(combination));
                }
                LocalDate inDate = plantShipmentReasonNew.getDate().plusDays(plantShipmentReasonNew.getLeadtimeOrDefault());
                int inValue = movementService.calculateVolume(combination.atDate(inDate));
                result.add(new CellValueChangeEvent<>(LeadtimeCell.class, combination, plantShipmentReasonNew.getLeadtime()));
                result.add(new CellReadOnlyChangeEvent<>(LeadtimeCell.class, combination, false));
                result.add(new CellValueChangeEvent<>(InCell.class, combination.atDate(inDate), inValue));
            }
        } else {
            TerminalWarehouseReceiptReason terminalWarehouseReceiptReason = terminalWarehouseReceiptReasonRepository.findByParentReason(plantShipmentReason)
                    .orElseThrow(() -> new IllegalStateException("TerminalWarehouseReceiptReason not found"));
            Movement movement = movementRepository.findByReason(plantShipmentReason)
                    .orElseThrow(() -> new IllegalStateException("Movement not found"));
            
            if (newVolume != null && newVolume != 0) {
                plantShipmentReason.setVolume(newVolume);
                terminalWarehouseReceiptReason.setVolume(newVolume);
                movement.setVolume(newVolume);
                dataManager.save(plantShipmentReason, terminalWarehouseReceiptReason, movement);
            } else {
                dataManager.remove(plantShipmentReason, terminalWarehouseReceiptReason, movement);
                result.add(new CellValueChangeEvent<>(VolumeCell.class, combination, newVolume));
                result.add(new CellValueChangeEvent<>(LeadtimeCell.class, combination, null));
                result.add(new CellReadOnlyChangeEvent<>(LeadtimeCell.class, combination, true));
            }
            if (plantShipmentReason.isValid()) {
                result.add(stockService.recalculateStocksSince(combination));
            }
            LocalDate inDate = plantShipmentReason.getDate().plusDays(plantShipmentReason.getLeadtimeOrDefault());
            ExPlantCombination inCombination = combination.atDate(inDate);
            int inValue = movementService.calculateVolume(inCombination);
            result.add(new CellValueChangeEvent<>(InCell.class, inCombination, inValue));
        }
        return result;
    }
    
    public PlantShipmentReason create(ExPlantCombination combination) {
        PlantShipmentReason plantShipmentReason = metadata.create(PlantShipmentReason.class);
        plantShipmentReason.setOriginPlant(combination.getPlant());
        plantShipmentReason.setProduct(combination.getProduct());
        plantShipmentReason.setProductPackage(combination.getProductPackage());
        plantShipmentReason.setWarehouse(combination.getWarehouseTerminal());
        plantShipmentReason.setTransportType(combination.getTransportType());
        plantShipmentReason.setTrack(combination.getTrack());
        plantShipmentReason.setDate(combination.getDate());
        return plantShipmentReason;
    }
}
