package com.hexstyle.jmixspreadsheet.datasource;

import java.util.function.Consumer;

/**
 * Adapter that wraps a data container and provides change notifications.
 * <p>
 * This adapter subscribes to item and property changes in the underlying
 * data container and emits domain-level change events to registered listeners.
 *
 * @param <E> the entity type
 */
public interface DataSourceAdapter<E> {

    /**
     * Registers a listener for entity added events.
     *
     * @param listener the listener to notify when entities are added
     */
    void addEntityAddedListener(Consumer<E> listener);

    /**
     * Registers a listener for entity removed events.
     *
     * @param listener the listener to notify when entities are removed
     */
    void addEntityRemovedListener(Consumer<E> listener);

    /**
     * Registers a listener for entity property changed events.
     *
     * @param listener the listener to notify when entity properties change
     */
    void addEntityChangedListener(Consumer<E> listener);

    /**
     * Registers a listener for collection refresh events.
     * <p>
     * This is called when the entire collection is refreshed (e.g., when filters change
     * or when the data loader reloads all items). The listener should trigger a full
     * rerender of the spreadsheet.
     *
     * @param listener the listener to notify when the collection is refreshed
     */
    void addRefreshListener(Runnable listener);

    /**
     * Returns all entities currently in the data source.
     *
     * @return an iterable of all entities
     */
    Iterable<E> getEntities();

    /**
     * Cleans up resources and unsubscribes from the underlying data container.
     */
    void dispose();
}
