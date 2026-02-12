package com.example.scm.entity.trait;

public interface Archivable {
    
    void setIsArchived(Boolean isArchived);
    
    Boolean getIsArchived();
    
    default boolean isArchived() {
        return Boolean.TRUE.equals(getIsArchived());
    }
}
