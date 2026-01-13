package com.company.jmixspreadsheet.spreadsheet.diff;

import com.company.jmixspreadsheet.spreadsheet.index.LayoutIndex;

/**
 * Analyzes changes between entity states and determines layout deltas.
 * <p>
 * The change analyzer compares old and new entity states and determines
 * which cells need to be updated, cleared, or where rows need to be inserted/removed.
 *
 * @param <E> the entity type
 */
public interface ChangeAnalyzer<E> {

    /**
     * Analyzes changes to an entity and determines the layout delta.
     *
     * @param oldEntity the previous state of the entity (snapshot)
     * @param newEntity the current state of the entity
     * @param layoutIndex the layout index for mapping entities to cells
     * @return the layout delta describing the changes
     */
    LayoutDelta analyzeChanges(E oldEntity, E newEntity, LayoutIndex<E> layoutIndex);
}
