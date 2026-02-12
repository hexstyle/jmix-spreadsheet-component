package com.example.scm.entity.trait;

import java.time.LocalDate;

public interface Limitation {
    
    LocalDate getDateStart();
    
    void setDateStart(LocalDate dateStart);
    
    LocalDate getDateEnd();
    
    void setDateEnd(LocalDate dateEnd);
}
