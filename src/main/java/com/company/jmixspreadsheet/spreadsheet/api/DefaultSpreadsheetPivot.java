package com.company.jmixspreadsheet.spreadsheet.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Default immutable implementation of {@link SpreadsheetPivot}.
 *
 * @param <E> the entity type
 */
public class DefaultSpreadsheetPivot<E> implements SpreadsheetPivot<E> {

    private final List<PivotAxis<E>> rowAxes;
    private final List<PivotAxis<E>> columnAxes;
    private final List<PivotMeasure<E>> measures;
    private final PivotEditStrategy<E> editStrategy;
    private final Optional<Supplier<List<Object>>> rowCompletion;
    private final Optional<Supplier<List<Object>>> columnCompletion;

    public DefaultSpreadsheetPivot(
            List<PivotAxis<E>> rowAxes,
            List<PivotAxis<E>> columnAxes,
            List<PivotMeasure<E>> measures,
            PivotEditStrategy<E> editStrategy) {
        this(rowAxes, columnAxes, measures, editStrategy, Optional.empty(), Optional.empty());
    }

    public DefaultSpreadsheetPivot(
            List<PivotAxis<E>> rowAxes,
            List<PivotAxis<E>> columnAxes,
            List<PivotMeasure<E>> measures,
            PivotEditStrategy<E> editStrategy,
            Optional<Supplier<List<Object>>> rowCompletion,
            Optional<Supplier<List<Object>>> columnCompletion) {
        this.rowAxes = rowAxes != null ? Collections.unmodifiableList(rowAxes) : Collections.emptyList();
        this.columnAxes = columnAxes != null ? Collections.unmodifiableList(columnAxes) : Collections.emptyList();
        this.measures = measures != null ? Collections.unmodifiableList(measures) : Collections.emptyList();
        this.editStrategy = editStrategy;
        this.rowCompletion = rowCompletion != null ? rowCompletion : Optional.empty();
        this.columnCompletion = columnCompletion != null ? columnCompletion : Optional.empty();
    }

    @Override
    public List<PivotAxis<E>> getRowAxes() {
        return rowAxes;
    }

    @Override
    public List<PivotAxis<E>> getColumnAxes() {
        return columnAxes;
    }

    @Override
    public List<PivotMeasure<E>> getMeasures() {
        return measures;
    }

    @Override
    public PivotEditStrategy<E> getEditStrategy() {
        return editStrategy;
    }

    @Override
    public Optional<Supplier<List<Object>>> getRowCompletion() {
        return rowCompletion;
    }

    @Override
    public Optional<Supplier<List<Object>>> getColumnCompletion() {
        return columnCompletion;
    }
}
