package com.hexstyle.jmixspreadsheet.api;

import java.util.function.Function;

/**
 * Default immutable implementation of {@link PivotMeasure}.
 *
 * @param <E> the entity type
 */
public class DefaultPivotMeasure<E> implements PivotMeasure<E> {

    private final String id;
    private final String caption;
    private final Function<E, Number> valueProvider;
    private final AggregationType aggregation;
    private final Function<Iterable<Number>, Number> customAggregation;

    public DefaultPivotMeasure(
            String id,
            String caption,
            Function<E, Number> valueProvider,
            AggregationType aggregation,
            Function<Iterable<Number>, Number> customAggregation) {
        this.id = id;
        this.caption = caption;
        this.valueProvider = valueProvider;
        this.aggregation = aggregation;
        this.customAggregation = customAggregation;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public Function<E, Number> getValueProvider() {
        return valueProvider;
    }

    @Override
    public AggregationType getAggregation() {
        return aggregation;
    }

    @Override
    public Function<Iterable<Number>, Number> getCustomAggregation() {
        return customAggregation;
    }
}
