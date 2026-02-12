package com.example.scm.spreadsheet;

import com.example.scm.spreadsheet.combination.ExPlantCombination;
import com.example.scm.spreadsheet.combination.ExPlantGroup;
import com.example.scm.spreadsheet.event.CellEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessingResult<E extends ExPlantGroup> {
    
    private static final ProcessingResult<ExPlantGroup> EMPTY = new ProcessingResult<ExPlantGroup>(Collections.emptyList());
    
    private final List<CellEvent<?, ?, ?>> events;
    
    public ProcessingResult() {
        this(new ArrayList<>());
    }
    
    private ProcessingResult(List<CellEvent<?, ?, ?>> events) {
        this.events = events;
    }
    
    public static ProcessingResult<ExPlantGroup> empty() {
        return EMPTY;
    }
    
    public List<CellEvent<?, ?, ?>> getEvents() {
        return events;
    }
    
    public void add(CellEvent<?, ?, ?> event) {
        events.add(event);
    }
    
    public void add(List<CellEvent<?, ?, ?>> events) {
        this.events.addAll(events);
    }
    
    public void add(ProcessingResult<ExPlantCombination> result) {
        events.addAll(result.events);
    }
}
