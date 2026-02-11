package com.digtp.scm.service;

import com.digtp.scm.spreadsheet.ProcessingResult;
import com.digtp.scm.spreadsheet.combination.ExPlantCombination;

public interface ExPlantProcessor<V> {
    
    ProcessingResult<ExPlantCombination> process(ExPlantCombination combination, V oldValue, V newValue);
}
