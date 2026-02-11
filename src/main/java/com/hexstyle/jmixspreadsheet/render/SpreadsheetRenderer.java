package com.hexstyle.jmixspreadsheet.render;

import com.hexstyle.jmixspreadsheet.layout.SpreadsheetLayout;

/**
 * Renders a spreadsheet layout to the underlying spreadsheet component.
 * <p>
 * The renderer performs the initial render of the spreadsheet structure,
 * applying cell values, styles, and merged regions.
 *
 * @param <E> the entity type
 * @param <C> the component type
 */
public interface SpreadsheetRenderer<E, C> {

    /**
     * Renders the given layout to the spreadsheet component.
     *
     * @param component the spreadsheet component to render to
     * @param layout the layout structure to render
     */
    void render(C component, SpreadsheetLayout<E> layout);
}
