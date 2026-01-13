package com.company.jmixspreadsheet.spreadsheet.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Default immutable implementation of {@link SpreadsheetTableModel}.
 *
 * @param <E> the entity type
 */
public class DefaultSpreadsheetTableModel<E> implements SpreadsheetTableModel<E> {

    private final Class<E> entityClass;
    private final List<SpreadsheetColumn<E>> columns;
    private final Grouping grouping;
    private final Object filter;
    private final Sort sort;
    private final Optional<SpreadsheetPivot<E>> pivot;
    private final SpreadsheetInteractionHandler<E> interactionHandler;

    public DefaultSpreadsheetTableModel(
            Class<E> entityClass,
            List<SpreadsheetColumn<E>> columns,
            Grouping grouping,
            Object filter,
            Sort sort,
            Optional<SpreadsheetPivot<E>> pivot,
            SpreadsheetInteractionHandler<E> interactionHandler) {
        this.entityClass = entityClass;
        this.columns = columns != null ? Collections.unmodifiableList(columns) : Collections.emptyList();
        this.grouping = grouping;
        this.filter = filter;
        this.sort = sort;
        this.pivot = pivot != null ? pivot : Optional.empty();
        this.interactionHandler = interactionHandler;
    }

    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public List<SpreadsheetColumn<E>> getColumns() {
        return columns;
    }

    @Override
    public Grouping getGrouping() {
        return grouping;
    }

    @Override
    public Object getFilter() {
        return filter;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Optional<SpreadsheetPivot<E>> getPivot() {
        return pivot;
    }

    @Override
    public SpreadsheetInteractionHandler<E> getInteractionHandler() {
        return interactionHandler;
    }
}
