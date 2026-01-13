package com.company.jmixspreadsheet.spreadsheet.layout;

/**
 * Defines a merged region of cells in the spreadsheet.
 */
public interface MergedRegion {

    /**
     * Returns the first row index of the merged region (0-based).
     *
     * @return the first row index
     */
    int getFirstRow();

    /**
     * Returns the last row index of the merged region (0-based).
     *
     * @return the last row index
     */
    int getLastRow();

    /**
     * Returns the first column index of the merged region (0-based).
     *
     * @return the first column index
     */
    int getFirstColumn();

    /**
     * Returns the last column index of the merged region (0-based).
     *
     * @return the last column index
     */
    int getLastColumn();
}
