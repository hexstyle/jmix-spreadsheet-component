package com.example.scm.service;

import com.example.scm.spreadsheet.ProcessingResult;
import com.example.scm.spreadsheet.combination.ExPlantCombination;

public interface ExPlantProcessor<V> {
    
    ProcessingResult<ExPlantCombination> process(ExPlantCombination combination, V oldValue, V newValue);
}
