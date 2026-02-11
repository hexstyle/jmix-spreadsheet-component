package com.hexstyle.jmixspreadsheet.diff;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of layout validation that determines whether incremental updates
 * are possible or if a full rerender is required.
 * <p>
 * This result captures the validation state and the reasons why a full rerender
 * might be required (e.g., pivot axis key changed, sorting changed, structural invalidation).
 *
 * @param <E> the entity type
 */
public class LayoutValidationResult<E> {

    private final boolean requiresFullRerender;
    private final List<String> reasons;

    /**
     * Creates a validation result.
     *
     * @param requiresFullRerender whether a full rerender is required
     * @param reasons the list of reasons for requiring full rerender, or empty if not required
     */
    private LayoutValidationResult(boolean requiresFullRerender, List<String> reasons) {
        this.requiresFullRerender = requiresFullRerender;
        this.reasons = reasons != null ? Collections.unmodifiableList(reasons) : Collections.emptyList();
    }

    /**
     * Creates a validation result indicating that incremental updates are possible.
     *
     * @param <E> the entity type
     * @return a validation result indicating incremental updates are possible
     */
    public static <E> LayoutValidationResult<E> incrementalUpdate() {
        return new LayoutValidationResult<>(false, Collections.emptyList());
    }

    /**
     * Creates a validation result indicating that a full rerender is required.
     *
     * @param reason the reason why full rerender is required
     * @param <E> the entity type
     * @return a validation result indicating full rerender is required
     */
    public static <E> LayoutValidationResult<E> fullRerender(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
        return new LayoutValidationResult<>(true, Collections.singletonList(reason));
    }

    /**
     * Creates a validation result indicating that a full rerender is required.
     *
     * @param reasons the list of reasons why full rerender is required
     * @param <E> the entity type
     * @return a validation result indicating full rerender is required
     */
    public static <E> LayoutValidationResult<E> fullRerender(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("Reasons cannot be null or empty");
        }
        // Validate that no reason is null or empty
        for (String reason : reasons) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Reason cannot be null or empty");
            }
        }
        return new LayoutValidationResult<>(true, reasons);
    }

    /**
     * Returns whether a full rerender is required.
     *
     * @return {@code true} if full rerender is required, {@code false} if incremental updates are possible
     */
    public boolean requiresFullRerender() {
        return requiresFullRerender;
    }

    /**
     * Returns the list of reasons why a full rerender is required.
     * <p>
     * This list is empty if incremental updates are possible.
     *
     * @return the list of reasons, never {@code null}
     */
    public List<String> getReasons() {
        return reasons;
    }

    /**
     * Returns a formatted string describing the validation result.
     *
     * @return a formatted string
     */
    @Override
    public String toString() {
        if (!requiresFullRerender) {
            return "LayoutValidationResult{incrementalUpdate}";
        }
        return "LayoutValidationResult{fullRerender, reasons=" + reasons + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LayoutValidationResult<?> that = (LayoutValidationResult<?>) o;
        return requiresFullRerender == that.requiresFullRerender
                && Objects.equals(reasons, that.reasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiresFullRerender, reasons);
    }
}
