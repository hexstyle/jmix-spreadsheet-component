package com.hexstyle.jmixspreadsheet.datasource;

import java.util.*;
import java.util.function.Consumer;

/**
 * Adapter that wraps a CollectionContainer and provides change notifications.
 * <p>
 * This implementation subscribes to item and property changes in the underlying
 * CollectionContainer and emits domain-level change events to registered listeners.
 * It does not trigger rerenders or compute diffs - it only emits change events.
 *
 * @param <E> the entity type
 * @param <C> the container type (typically CollectionContainer)
 */
public class ContainerSpreadsheetDataSource<E, C> implements DataSourceAdapter<E> {

    private final C container;
    private final ContainerAdapter<C, E> containerAdapter;

    private final List<Consumer<E>> entityAddedListeners = new ArrayList<>();
    private final List<Consumer<E>> entityRemovedListeners = new ArrayList<>();
    private final List<Consumer<E>> entityChangedListeners = new ArrayList<>();
    private final List<Runnable> refreshListeners = new ArrayList<>();

    private boolean disposed = false;

    /**
     * Creates a new data source adapter for the given container.
     *
     * @param container the collection container to wrap
     * @param containerAdapter adapter for accessing container operations
     */
    public ContainerSpreadsheetDataSource(
            C container,
            ContainerAdapter<C, E> containerAdapter) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null");
        }
        if (containerAdapter == null) {
            throw new IllegalArgumentException("Container adapter cannot be null");
        }

        this.container = container;
        this.containerAdapter = containerAdapter;

        // Subscribe to container events
        subscribeToContainer();
    }

    @Override
    public void addEntityAddedListener(Consumer<E> listener) {
        if (disposed) {
            throw new IllegalStateException("Data source adapter is disposed");
        }
        if (listener != null) {
            entityAddedListeners.add(listener);
        }
    }

    @Override
    public void addEntityRemovedListener(Consumer<E> listener) {
        if (disposed) {
            throw new IllegalStateException("Data source adapter is disposed");
        }
        if (listener != null) {
            entityRemovedListeners.add(listener);
        }
    }

    @Override
    public void addEntityChangedListener(Consumer<E> listener) {
        if (disposed) {
            throw new IllegalStateException("Data source adapter is disposed");
        }
        if (listener != null) {
            entityChangedListeners.add(listener);
        }
    }

    @Override
    public void addRefreshListener(Runnable listener) {
        if (disposed) {
            throw new IllegalStateException("Data source adapter is disposed");
        }
        if (listener != null) {
            refreshListeners.add(listener);
        }
    }

    @Override
    public Iterable<E> getEntities() {
        if (disposed) {
            throw new IllegalStateException("Data source adapter is disposed");
        }
        return containerAdapter.getItems(container);
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }

        // Unsubscribe from container events
        containerAdapter.unsubscribe(container);

        // Clear listeners
        entityAddedListeners.clear();
        entityRemovedListeners.clear();
        entityChangedListeners.clear();
        refreshListeners.clear();

        disposed = true;
    }

    // Private helper methods

    private void subscribeToContainer() {
        containerAdapter.subscribe(
                container,
                this::onItemAdded,
                this::onItemRemoved,
                this::onItemChanged,
                this::onRefresh
        );
    }

    private void onItemAdded(E entity) {
        if (!disposed) {
            for (Consumer<E> listener : entityAddedListeners) {
                listener.accept(entity);
            }
        }
    }

    private void onItemRemoved(E entity) {
        if (!disposed) {
            for (Consumer<E> listener : entityRemovedListeners) {
                listener.accept(entity);
            }
        }
    }

    private void onItemChanged(E entity) {
        if (!disposed) {
            for (Consumer<E> listener : entityChangedListeners) {
                listener.accept(entity);
            }
        }
    }

    private void onRefresh() {
        if (!disposed) {
            for (Runnable listener : refreshListeners) {
                listener.run();
            }
        }
    }

    /**
     * Adapter interface for accessing container operations.
     * <p>
     * This abstraction allows the data source adapter to work with different
     * container types without direct dependencies on specific container implementations.
     *
     * @param <C> the container type
     * @param <E> the entity type
     */
    public interface ContainerAdapter<C, E> {
        /**
         * Returns all items in the container.
         *
         * @param container the container
         * @return an iterable of all items
         */
        Iterable<E> getItems(C container);

        /**
         * Subscribes to container events.
         *
         * @param container the container
         * @param itemAddedHandler handler for item added events
         * @param itemRemovedHandler handler for item removed events
         * @param itemChangedHandler handler for item property changed events
         * @param refreshHandler handler for collection refresh events (e.g., when filters change)
         */
        void subscribe(C container, Consumer<E> itemAddedHandler, Consumer<E> itemRemovedHandler, Consumer<E> itemChangedHandler, Runnable refreshHandler);

        /**
         * Unsubscribes from container events.
         *
         * @param container the container
         */
        void unsubscribe(C container);
    }
}
