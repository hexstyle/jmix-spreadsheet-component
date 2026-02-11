package com.hexstyle.jmixspreadsheet.ui.component;

import com.hexstyle.jmixspreadsheet.api.SpreadsheetController;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Jmix FlowUI component wrapper for Vaadin Spreadsheet.
 * <p>
 * This component wraps the Vaadin Spreadsheet component and delegates all logic
 * to the existing SpreadsheetController. It contains no business logic and serves
 * as a thin adapter layer between FlowUI XML and the controller API.
 * <p>
 * The component is designed to be used declaratively in XML views with data binding.
 *
 * @param <E> the entity type
 */
public class SpreadsheetComponent<E> extends Composite<Div> implements HasSize {

    private SpreadsheetController<E, ?> controller;
    private Spreadsheet spreadsheet;
    private boolean readOnly = true;

    /**
     * Creates a new spreadsheet component.
     */
    public SpreadsheetComponent() {
        // Component will be initialized via setController()
    }

    @Override
    protected Div initContent() {
        Div content = super.initContent();
        return content;
    }

    /**
     * Sets the spreadsheet controller that manages this component.
     * <p>
     * This method should be called by the component loader during initialization.
     * The controller manages all business logic and data binding.
     *
     * @param controller the spreadsheet controller
     */
    public void setController(SpreadsheetController<E, ?> controller) {
        this.controller = controller;
        if (controller != null) {
            this.spreadsheet = controller.getComponent();
            controller.setReadOnly(readOnly);
            // Attach the Vaadin Spreadsheet component to the content div
            getContent().add(spreadsheet);
        }
    }

    /**
     * Returns the spreadsheet controller.
     *
     * @return the controller, or {@code null} if not set
     */
    public SpreadsheetController<E, ?> getController() {
        return controller;
    }

    /**
     * Returns the underlying Vaadin Spreadsheet component.
     * <p>
     * This method provides access to the wrapped Vaadin component.
     * The component is available after the controller has been set and bound.
     *
     * @return the Vaadin Spreadsheet component, or {@code null} if not yet initialized
     */
    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    /**
     * Returns whether the spreadsheet is read-only.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets read-only mode for the spreadsheet.
     * <p>
     * When enabled, any cell edits are rejected and reverted to their previous values.
     *
     * @param readOnly whether the spreadsheet is read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (controller != null) {
            controller.setReadOnly(readOnly);
        }
    }

    /**
     * Reloads the spreadsheet data.
     * <p>
     * This method delegates to the controller's reload() method.
     *
     * @throws IllegalStateException if the controller is not bound
     */
    public void reload() {
        if (controller == null) {
            throw new IllegalStateException("Controller is not set");
        }
        controller.reload();
    }
}
