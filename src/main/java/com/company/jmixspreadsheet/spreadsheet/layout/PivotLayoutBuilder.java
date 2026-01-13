package com.company.jmixspreadsheet.spreadsheet.layout;

import com.company.jmixspreadsheet.spreadsheet.api.PivotAxis;
import com.company.jmixspreadsheet.spreadsheet.api.PivotMeasure;
import com.company.jmixspreadsheet.spreadsheet.api.SpreadsheetPivot;
import com.company.jmixspreadsheet.spreadsheet.api.SpreadsheetTableModel;
import com.company.jmixspreadsheet.spreadsheet.api.SpreadsheetColumn;

import java.util.*;
import java.util.function.Supplier;

/**
 * Builds pivot table layouts from entities.
 * <p>
 * This builder creates pivot table structures by:
 * - Building row and column axis trees (hierarchical grouping)
 * - Computing aggregated measures at intersections
 * - Creating merged header regions based on render modes
 * - Producing PivotCellBinding instances with source entities
 * <p>
 * This builder does not handle diff or editing logic - only layout building.
 *
 * @param <E> the entity type
 */
public class PivotLayoutBuilder<E> implements LayoutEngine<E> {

    @Override
    public SpreadsheetLayout<E> buildLayout(SpreadsheetTableModel<E> model, Iterable<E> entities) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (entities == null) {
            throw new IllegalArgumentException("Entities cannot be null");
        }

        SpreadsheetPivot<E> pivot = model.getPivot().orElseThrow(
                () -> new IllegalArgumentException("Model does not have a pivot configuration")
        );

        // Convert entities to list for processing
        List<E> entityList = new ArrayList<>();
        entities.forEach(entityList::add);

        // Build pivot layout (include regular columns from model)
        return buildPivotLayout(model, pivot, entityList);
    }

    /**
     * Builds a pivot table layout from entities.
     */
    private SpreadsheetLayout<E> buildPivotLayout(
            SpreadsheetTableModel<E> model,
            SpreadsheetPivot<E> pivot,
            List<E> entities) {
        List<CellBinding<E>> cellBindings = new ArrayList<>();
        List<MergedRegion> mergedRegions = new ArrayList<>();

        // Get regular columns (non-pivot columns) from model
        List<SpreadsheetColumn<E>> regularColumns = model.getColumns() != null ? model.getColumns() : Collections.emptyList();
        int regularColCount = regularColumns.size();

        // Sort entities by flat column values when there are no row axes but regular columns exist
        // This ensures entities with the same flat column values remain in stable positions
        List<E> sortedEntities = entities;
        if (pivot.getRowAxes().isEmpty() && !regularColumns.isEmpty()) {
            sortedEntities = sortEntitiesByFlatColumns(new ArrayList<>(entities), regularColumns);
        }

        // Build row axis tree (with optional row completion)
        AxisNode<E> rowTree = buildAxisTree(
                pivot.getRowAxes(), 
                sortedEntities, 
                pivot.getRowCompletion());

        // Build column axis tree (with optional column completion)
        AxisNode<E> columnTree = buildAxisTree(
                pivot.getColumnAxes(), 
                entities,
                pivot.getColumnCompletion());

        // Calculate layout dimensions
        int rowHeaderCols = calculateHeaderDepth(pivot.getRowAxes());
        int colHeaderRows = calculateHeaderDepth(pivot.getColumnAxes());
        // Add one row for measure headers if measures exist
        if (!pivot.getMeasures().isEmpty()) {
            colHeaderRows++;
        }

        // Calculate total rows: if no row axes but regular columns exist, group entities by flat column values
        int dataRowCount;
        if (pivot.getRowAxes().isEmpty() && !regularColumns.isEmpty()) {
            // Group entities by flat column values - one row per unique combination of flat column values
            Map<List<Object>, List<E>> groupedByFlatColumns = groupEntitiesByFlatColumns(sortedEntities, regularColumns);
            dataRowCount = groupedByFlatColumns.size();
        } else {
            // Use row tree leaves (aggregated rows)
            dataRowCount = rowTree.getTotalLeaves();
        }
        
        int totalRows = colHeaderRows + dataRowCount;
        int totalCols = regularColCount + rowHeaderCols + (columnTree.getTotalLeaves() * pivot.getMeasures().size());

        // Build pivot column headers first (to determine the last header row)
        int pivotHeaderEndRow = buildColumnHeaders(columnTree, pivot, regularColCount + rowHeaderCols, 0, cellBindings, mergedRegions);
        
        // Build row axis labels in the column header area (top-left corner)
        if (rowHeaderCols > 0 && !pivot.getRowAxes().isEmpty()) {
            buildRowAxisLabels(pivot.getRowAxes(), colHeaderRows, regularColCount, cellBindings, mergedRegions);
        }
        
        // Build regular column headers in the last header row (same row as last pivot header)
        if (!regularColumns.isEmpty()) {
            int lastHeaderRow = pivotHeaderEndRow - 1; // Last row of pivot headers
            buildRegularColumnHeadersInRow(regularColumns, lastHeaderRow, cellBindings, mergedRegions);
        }
        
        int currentRow = pivotHeaderEndRow;

        // Build row headers and data cells (including regular columns)
        buildRowHeadersAndData(rowTree, columnTree, pivot, regularColumns, regularColCount, rowHeaderCols, currentRow, cellBindings, mergedRegions);

        return new DefaultSpreadsheetLayout<>(totalRows, totalCols, cellBindings, mergedRegions);
    }

    /**
     * Represents a node in the axis tree.
     */
    private static class AxisNode<E> {
        private Object key;
        private final List<E> entities;
        private final Map<Object, AxisNode<E>> children = new LinkedHashMap<>();
        private final int level;

        AxisNode(Object key, List<E> entities, int level) {
            this.key = key;
            this.entities = entities;
            this.level = level;
        }

        Object getKey() {
            return key;
        }

        void setKey(Object key) {
            this.key = key;
        }

        List<E> getEntities() {
            return entities;
        }

        Collection<AxisNode<E>> getChildren() {
            return children.values();
        }

        void addChild(AxisNode<E> child) {
            children.put(child.key, child);
        }

        int getLevel() {
            return level;
        }

        int getTotalLeaves() {
            if (children.isEmpty()) {
                return 1;
            }
            return children.values().stream()
                    .mapToInt(AxisNode::getTotalLeaves)
                    .sum();
        }
    }

    /**
     * Builds an axis tree from a list of axes and entities.
     */
    private AxisNode<E> buildAxisTree(List<PivotAxis<E>> axes, List<E> entities) {
        return buildAxisTree(axes, entities, Optional.empty());
    }

    /**
     * Builds an axis tree from a list of axes and entities, with optional completion supplier.
     * <p>
     * When a completion supplier is provided for the first axis level, it will generate
     * all values from the supplier, even if no entities exist for those values.
     *
     * @param axes the axis definitions
     * @param entities the entities to group
     * @param completion optional supplier that provides complete set of values for first axis level
     * @return the axis tree root node
     */
    private AxisNode<E> buildAxisTree(
            List<PivotAxis<E>> axes, 
            List<E> entities,
            Optional<Supplier<List<Object>>> completion) {
        if (axes.isEmpty()) {
            // Root node with all entities
            return new AxisNode<>(null, entities, 0);
        }

        return buildAxisTreeRecursive(axes, entities, 0, completion);
    }

    /**
     * Recursively builds axis tree nodes.
     */
    private AxisNode<E> buildAxisTreeRecursive(List<PivotAxis<E>> axes, List<E> entities, int level) {
        return buildAxisTreeRecursive(axes, entities, level, Optional.empty());
    }

    /**
     * Recursively builds axis tree nodes, with optional completion support.
     *
     * @param axes the axis definitions
     * @param entities the entities to group
     * @param level the current axis level (0-based)
     * @param completion optional supplier that provides complete set of values for first axis level
     * @return the axis tree node
     */
    private AxisNode<E> buildAxisTreeRecursive(
            List<PivotAxis<E>> axes, 
            List<E> entities, 
            int level,
            Optional<Supplier<List<Object>>> completion) {
        if (level >= axes.size()) {
            // Leaf node
            return new AxisNode<>(null, entities, level);
        }

        PivotAxis<E> axis = axes.get(level);
        
        // Group entities by axis key
        Map<Object, List<E>> grouped = groupByKey(axis, entities);

        // Get keys - use completion supplier if provided for first level
        List<Object> sortedKeys;
        if (level == 0 && completion.isPresent()) {
            // Get complete set of values from completion supplier
            List<Object> completeValues = completion.get().get();
            sortedKeys = new ArrayList<>(completeValues);
            
            // Merge with values from entities (in case completion misses some)
            for (Object key : grouped.keySet()) {
                if (!sortedKeys.contains(key)) {
                    sortedKeys.add(key);
                }
            }
        } else {
            // Use only values from entities
            sortedKeys = new ArrayList<>(grouped.keySet());
        }

        // Sort keys
        Comparator<Object> comparator = axis.getComparator();
        if (comparator != null) {
            sortedKeys.sort(comparator);
        } else {
            sortedKeys.sort((a, b) -> {
                if (a == null && b == null) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                if (a instanceof Comparable && b instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> ca = (Comparable<Object>) a;
                    return ca.compareTo(b);
                }
                return a.toString().compareTo(b.toString());
            });
        }

        // Create root node for this level
        AxisNode<E> root = new AxisNode<>(null, entities, level);

        // Build children
        for (Object key : sortedKeys) {
            // Get entities for this key (may be empty if from completion supplier)
            List<E> childEntities = grouped.getOrDefault(key, Collections.emptyList());
            AxisNode<E> child = buildAxisTreeRecursive(axes, childEntities, level + 1, Optional.empty());
            child.setKey(key);
            root.addChild(child);
        }

        return root;
    }

    /**
     * Groups entities by axis key.
     */
    private Map<Object, List<E>> groupByKey(PivotAxis<E> axis, List<E> entities) {
        Map<Object, List<E>> grouped = new LinkedHashMap<>();
        for (E entity : entities) {
            Object key = axis.getKeyProvider().apply(entity);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
        }
        return grouped;
    }

    /**
     * Calculates the depth of header columns/rows needed.
     */
    private int calculateHeaderDepth(List<PivotAxis<E>> axes) {
        return axes.size();
    }

    /**
     * Builds column headers.
     */
    private int buildColumnHeaders(
            AxisNode<E> columnTree,
            SpreadsheetPivot<E> pivot,
            int rowHeaderCols,
            int startRow,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {

        List<PivotAxis<E>> columnAxes = pivot.getColumnAxes();
        List<PivotMeasure<E>> measures = pivot.getMeasures();

        int currentRow = startRow;

        // Build headers for each axis level
        for (int axisLevel = 0; axisLevel < columnAxes.size(); axisLevel++) {
            PivotAxis<E> axis = columnAxes.get(axisLevel);
            int currentCol = rowHeaderCols;

            buildColumnHeaderLevel(columnTree, axisLevel, columnAxes.size(), measures.size(),
                    rowHeaderCols, currentRow, currentCol, axis, cellBindings, mergedRegions);

            currentRow++;
        }

        // Build measure headers as a row after all column axis headers
        if (!measures.isEmpty()) {
            buildMeasureHeaders(columnTree, measures, rowHeaderCols, currentRow, cellBindings, mergedRegions);
            currentRow++;
        }

        return currentRow;
    }

    /**
     * Builds a level of column headers.
     */
    private void buildColumnHeaderLevel(
            AxisNode<E> node,
            int targetLevel,
            int totalLevels,
            int measureCount,
            int rowHeaderCols,
            int row,
            int startCol,
            PivotAxis<E> axis,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {

        if (node.getLevel() == targetLevel) {
            // Build headers for this node's children
            int currentCol = startCol;
            for (AxisNode<E> child : node.getChildren()) {
                int span = child.getTotalLeaves() * measureCount;
                String headerText = formatKey(child.getKey());

                // Create header cell binding
                PivotCellBinding<E> binding = new PivotCellBinding<>(
                        row, currentCol, headerText, null, Collections.emptyList());
                cellBindings.add(binding);

                // Create merged region if needed
                if (axis.getRenderMode() == PivotAxis.RenderMode.MERGED && span > 1) {
                    mergedRegions.add(new DefaultMergedRegion(row, row, currentCol, currentCol + span - 1));
                }

                currentCol += span;
            }
        } else if (node.getLevel() < targetLevel) {
            // Recurse to children
            for (AxisNode<E> child : node.getChildren()) {
                int span = child.getTotalLeaves() * measureCount;
                buildColumnHeaderLevel(child, targetLevel, totalLevels, measureCount,
                        rowHeaderCols, row, startCol, axis, cellBindings, mergedRegions);
                startCol += span;
            }
        }
    }

    /**
     * Builds measure headers as a row after all column axis headers.
     */
    private void buildMeasureHeaders(
            AxisNode<E> columnTree,
            List<PivotMeasure<E>> measures,
            int rowHeaderCols,
            int row,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {
        
        buildMeasureHeadersRecursive(columnTree, measures, rowHeaderCols, row, rowHeaderCols, cellBindings, mergedRegions);
    }

    /**
     * Recursively builds measure header cells for the column tree.
     */
    private int buildMeasureHeadersRecursive(
            AxisNode<E> columnNode,
            List<PivotMeasure<E>> measures,
            int rowHeaderCols,
            int row,
            int startCol,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {

        if (columnNode.getChildren().isEmpty()) {
            // Leaf - build measure header cells
            int currentCol = startCol;
            for (PivotMeasure<E> measure : measures) {
                String headerText = measure.getCaption();
                
                // Create header cell binding
                PivotCellBinding<E> binding = new PivotCellBinding<>(
                        row, currentCol, headerText, null, Collections.emptyList());
                cellBindings.add(binding);
                currentCol++;
            }
            return currentCol;
        } else {
            // Recurse to children
            int currentCol = startCol;
            for (AxisNode<E> child : columnNode.getChildren()) {
                currentCol = buildMeasureHeadersRecursive(child, measures, rowHeaderCols, row, currentCol, cellBindings, mergedRegions);
            }
            return currentCol;
        }
    }

    /**
     * Builds row headers and data cells.
     */
    private void buildRowHeadersAndData(
            AxisNode<E> rowTree,
            AxisNode<E> columnTree,
            SpreadsheetPivot<E> pivot,
            List<SpreadsheetColumn<E>> regularColumns,
            int regularColCount,
            int rowHeaderCols,
            int startRow,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {

        List<PivotMeasure<E>> measures = pivot.getMeasures();
        int currentRow = startRow;

        buildRowHeadersAndDataRecursive(rowTree, columnTree, pivot, regularColumns, regularColCount, rowHeaderCols, currentRow,
                0, measures, cellBindings, mergedRegions);
    }

    /**
     * Recursively builds row headers and data cells.
     */
    private int buildRowHeadersAndDataRecursive(
            AxisNode<E> rowNode,
            AxisNode<E> columnTree,
            SpreadsheetPivot<E> pivot,
            List<SpreadsheetColumn<E>> regularColumns,
            int regularColCount,
            int rowHeaderCols,
            int startRow,
            int headerCol,
            List<PivotMeasure<E>> measures,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {

        if (rowNode.getChildren().isEmpty()) {
            // Leaf node - build data row(s)
            // Special case: if no row axes but regular columns exist, group entities by flat column values
            if (pivot.getRowAxes().isEmpty() && !regularColumns.isEmpty()) {
                return buildDataRowsGroupedByFlatColumns(rowNode, columnTree, pivot, regularColumns, regularColCount, rowHeaderCols, startRow, headerCol, measures, cellBindings);
            } else {
                return buildDataRow(rowNode, columnTree, pivot, regularColumns, regularColCount, rowHeaderCols, startRow, headerCol, measures, cellBindings);
            }
        } else {
            // Internal node - build headers and recurse
            int currentRow = startRow;
            for (AxisNode<E> child : rowNode.getChildren()) {
                // Build header cell
                String headerText = formatKey(child.getKey());
                PivotCellBinding<E> headerBinding = new PivotCellBinding<>(
                        currentRow, regularColCount + headerCol, headerText, null, Collections.emptyList());
                cellBindings.add(headerBinding);

                // Recurse to child
                currentRow = buildRowHeadersAndDataRecursive(child, columnTree, pivot, regularColumns, regularColCount, rowHeaderCols,
                        currentRow, headerCol + 1, measures, cellBindings, mergedRegions);
            }
            return currentRow;
        }
    }

    /**
     * Builds a data row with measure values.
     */
    private int buildDataRow(
            AxisNode<E> rowNode,
            AxisNode<E> columnTree,
            SpreadsheetPivot<E> pivot,
            List<SpreadsheetColumn<E>> regularColumns,
            int regularColCount,
            int rowHeaderCols,
            int row,
            int headerCol,
            List<PivotMeasure<E>> measures,
            List<CellBinding<E>> cellBindings) {

        List<E> rowEntities = rowNode.getEntities();
        int currentCol = 0;

        // Build regular column cells first
        if (!regularColumns.isEmpty() && !rowEntities.isEmpty()) {
            // Use first entity for regular column values (in a pivot, this represents the row)
            E firstEntity = rowEntities.get(0);
            for (SpreadsheetColumn<E> column : regularColumns) {
                Object value = column.getValueProvider().apply(firstEntity);
                String displayValue = formatValue(value, column.getFormatter());
                CellBinding<E> binding = new DefaultCellBinding<>(
                        row, currentCol, displayValue, null, firstEntity);
                cellBindings.add(binding);
                currentCol++;
            }
        }

        // Build measure cells for each column leaf
        currentCol = regularColCount + rowHeaderCols;
        buildDataRowRecursive(columnTree, rowEntities, measures, row, currentCol, cellBindings);

        return row + 1;
    }

    /**
     * Builds data rows grouped by flat column values when there are no row axes but regular columns exist.
     * This creates one row per unique combination of flat column values, with aggregated pivot measure values.
     */
    private int buildDataRowsGroupedByFlatColumns(
            AxisNode<E> rowNode,
            AxisNode<E> columnTree,
            SpreadsheetPivot<E> pivot,
            List<SpreadsheetColumn<E>> regularColumns,
            int regularColCount,
            int rowHeaderCols,
            int startRow,
            int headerCol,
            List<PivotMeasure<E>> measures,
            List<CellBinding<E>> cellBindings) {

        List<E> allEntities = rowNode.getEntities();
        
        // Group entities by flat column values
        Map<List<Object>, List<E>> groupedByFlatColumns = groupEntitiesByFlatColumns(allEntities, regularColumns);
        
        // Sort groups by flat column values for stable ordering
        List<Map.Entry<List<Object>, List<E>>> sortedGroups = new ArrayList<>(groupedByFlatColumns.entrySet());
        sortedGroups.sort((e1, e2) -> {
            List<Object> values1 = e1.getKey();
            List<Object> values2 = e2.getKey();
            for (int i = 0; i < Math.min(values1.size(), values2.size()); i++) {
                int comparison = compareValues(values1.get(i), values2.get(i));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(values1.size(), values2.size());
        });

        int currentRow = startRow;

        // Create a row for each group of entities with the same flat column values
        for (Map.Entry<List<Object>, List<E>> group : sortedGroups) {
            List<E> groupEntities = group.getValue();
            
            // Create a temporary AxisNode for this group to reuse buildDataRow logic
            AxisNode<E> groupNode = new AxisNode<>(null, groupEntities, 0);
            
            // Build one row for this group (buildDataRow will aggregate all entities in the group)
            currentRow = buildDataRow(groupNode, columnTree, pivot, regularColumns, regularColCount, rowHeaderCols, currentRow, headerCol, measures, cellBindings);
        }

        return currentRow;
    }

    /**
     * Groups entities by their flat column values.
     * <p>
     * Returns a map where the key is a list of flat column values (in order) and the value
     * is a list of entities that have those exact flat column values.
     *
     * @param entities the entities to group
     * @param flatColumns the flat columns to group by
     * @return a map of flat column value combinations to lists of entities
     */
    private Map<List<Object>, List<E>> groupEntitiesByFlatColumns(List<E> entities, List<SpreadsheetColumn<E>> flatColumns) {
        Map<List<Object>, List<E>> grouped = new LinkedHashMap<>();
        
        for (E entity : entities) {
            // Extract flat column values for this entity
            List<Object> key = new ArrayList<>();
            for (SpreadsheetColumn<E> column : flatColumns) {
                Object value = column.getValueProvider().apply(entity);
                key.add(value);
            }
            
            // Group entities by this key
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
        }
        
        return grouped;
    }

    /**
     * Recursively builds data cells for column tree.
     */
    private int buildDataRowRecursive(
            AxisNode<E> columnNode,
            List<E> rowEntities,
            List<PivotMeasure<E>> measures,
            int row,
            int startCol,
            List<CellBinding<E>> cellBindings) {

        if (columnNode.getChildren().isEmpty()) {
            // Leaf - build measure cells
            List<E> intersectionEntities = filterEntities(rowEntities, columnNode.getEntities());
            int currentCol = startCol;

            for (PivotMeasure<E> measure : measures) {
                Object aggregatedValue = computeMeasure(measure, intersectionEntities);
                PivotCellBinding<E> binding = new PivotCellBinding<>(
                        row, currentCol, aggregatedValue, null, intersectionEntities);
                cellBindings.add(binding);
                currentCol++;
            }

            return currentCol;
        } else {
            // Recurse to children
            int currentCol = startCol;
            for (AxisNode<E> child : columnNode.getChildren()) {
                currentCol = buildDataRowRecursive(child, rowEntities, measures, row, currentCol, cellBindings);
            }
            return currentCol;
        }
    }

    /**
     * Filters entities that are in both lists.
     */
    private List<E> filterEntities(List<E> rowEntities, List<E> columnEntities) {
        Set<E> columnSet = new HashSet<>(columnEntities);
        List<E> result = new ArrayList<>();
        for (E entity : rowEntities) {
            if (columnSet.contains(entity)) {
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * Computes an aggregated measure value.
     */
    private Object computeMeasure(PivotMeasure<E> measure, List<E> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        List<Number> values = new ArrayList<>();
        for (E entity : entities) {
            Number value = measure.getValueProvider().apply(entity);
            if (value != null) {
                values.add(value);
            }
        }

        if (values.isEmpty()) {
            return null;
        }

        switch (measure.getAggregation()) {
            case SUM:
                return values.stream()
                        .mapToDouble(Number::doubleValue)
                        .sum();
            case COUNT:
                return (long) values.size();
            case AVG:
                double sum = values.stream().mapToDouble(Number::doubleValue).sum();
                return sum / values.size();
            case CUSTOM:
                java.util.function.Function<Iterable<Number>, Number> customAgg = measure.getCustomAggregation();
                if (customAgg != null) {
                    return customAgg.apply(values);
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * Sorts entities by their flat column values to ensure stable ordering.
     * <p>
     * Entities are sorted by all flat columns in order (like SQL ORDER BY col1, col2, ...).
     * This ensures that entities with the same flat column values remain in stable positions
     * after edits and page reloads.
     * <p>
     * Uses a stable sort (merge sort) to preserve relative order of entities with identical values.
     *
     * @param entities the entities to sort
     * @param flatColumns the flat columns (regular columns) to sort by
     * @return a new sorted list of entities
     */
    private List<E> sortEntitiesByFlatColumns(List<E> entities, List<SpreadsheetColumn<E>> flatColumns) {
        if (flatColumns.isEmpty() || entities.isEmpty()) {
            return entities;
        }

        // Create a comparator that compares entities by all flat columns in order
        Comparator<E> comparator = (e1, e2) -> {
            for (SpreadsheetColumn<E> column : flatColumns) {
                Object value1 = column.getValueProvider().apply(e1);
                Object value2 = column.getValueProvider().apply(e2);

                int comparison = compareValues(value1, value2);
                if (comparison != 0) {
                    return comparison;
                }
            }
            // All values are equal - maintain relative order (stable sort)
            return 0;
        };

        // Use Collections.sort which uses merge sort (stable sort)
        // This preserves the relative order of entities with identical flat column values
        Collections.sort(entities, comparator);
        return entities;
    }

    /**
     * Compares two values for sorting.
     * <p>
     * Handles null values (nulls come last) and Comparable types.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return a negative integer, zero, or a positive integer as the first value is less than,
     *         equal to, or greater than the second value
     */
    @SuppressWarnings("unchecked")
    private int compareValues(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return 0;
        }
        if (value1 == null) {
            return 1; // null comes last
        }
        if (value2 == null) {
            return -1; // null comes last
        }
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            try {
                return ((Comparable<Object>) value1).compareTo(value2);
            } catch (ClassCastException e) {
                // Fall back to string comparison if types are incompatible
                return value1.toString().compareTo(value2.toString());
            }
        }
        // Fall back to string comparison
        return value1.toString().compareTo(value2.toString());
    }

    /**
     * Formats a key value for display.
     */
    private String formatKey(Object key) {
        if (key == null) {
            return "";
        }
        return key.toString();
    }

    /**
     * Formats a value using the column's formatter, if available.
     */
    private String formatValue(Object value, java.util.function.Function<Object, String> formatter) {
        if (formatter != null) {
            return formatter.apply(value);
        }
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    /**
     * Builds row axis labels in the column header area (top-left corner).
     * These labels indicate what each row header column represents.
     * Labels are placed in the last header row, aligned with the row header columns.
     */
    private void buildRowAxisLabels(
            List<PivotAxis<E>> rowAxes,
            int colHeaderRows,
            int regularColCount,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {
        
        if (rowAxes.isEmpty() || colHeaderRows == 0) {
            return;
        }
        
        // Place labels in the last header row (same row as the last column header level)
        int labelRow = colHeaderRows - 1;
        
        // Build labels for each row axis level
        for (int axisLevel = 0; axisLevel < rowAxes.size(); axisLevel++) {
            PivotAxis<E> axis = rowAxes.get(axisLevel);
            String axisLabel = axis.getId(); // Use axis ID as label (e.g., "day")
            
            // Create label cell in the row header column area (top-left corner)
            // Position: row = last header row, column = regularColCount + axisLevel
            PivotCellBinding<E> labelBinding = new PivotCellBinding<>(
                    labelRow, regularColCount + axisLevel, axisLabel, null, Collections.emptyList());
            cellBindings.add(labelBinding);
        }
    }

    /**
     * Builds column headers for regular (non-pivot) columns in a specific row.
     * This is used to align regular column headers with pivot column headers.
     */
    private void buildRegularColumnHeadersInRow(
            List<SpreadsheetColumn<E>> regularColumns,
            int row,
            List<CellBinding<E>> cellBindings,
            List<MergedRegion> mergedRegions) {

        int currentCol = 0;

        // Build headers in the specified row
        for (SpreadsheetColumn<E> column : regularColumns) {
            CellBinding<E> binding = new DefaultCellBinding<>(
                    row, currentCol, column.getHeader(), null, null);
            cellBindings.add(binding);
            currentCol++;
        }
    }

    // Supporting classes

    /**
     * Default implementation of MergedRegion.
     */
    private static class DefaultMergedRegion implements MergedRegion {
        private final int firstRow;
        private final int lastRow;
        private final int firstColumn;
        private final int lastColumn;

        DefaultMergedRegion(int firstRow, int lastRow, int firstColumn, int lastColumn) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
        }

        @Override
        public int getFirstRow() {
            return firstRow;
        }

        @Override
        public int getLastRow() {
            return lastRow;
        }

        @Override
        public int getFirstColumn() {
            return firstColumn;
        }

        @Override
        public int getLastColumn() {
            return lastColumn;
        }
    }

    /**
     * Default implementation of SpreadsheetLayout.
     */
    private static class DefaultSpreadsheetLayout<E> implements SpreadsheetLayout<E> {
        private final int rowCount;
        private final int columnCount;
        private final List<CellBinding<E>> cellBindings;
        private final List<MergedRegion> mergedRegions;

        DefaultSpreadsheetLayout(int rowCount, int columnCount,
                                List<CellBinding<E>> cellBindings,
                                List<MergedRegion> mergedRegions) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.cellBindings = cellBindings != null ? new ArrayList<>(cellBindings) : new ArrayList<>();
            this.mergedRegions = mergedRegions != null ? new ArrayList<>(mergedRegions) : new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            return rowCount;
        }

        @Override
        public int getColumnCount() {
            return columnCount;
        }

        @Override
        public List<CellBinding<E>> getCellBindings() {
            return Collections.unmodifiableList(cellBindings);
        }

        @Override
        public List<MergedRegion> getMergedRegions() {
            return Collections.unmodifiableList(mergedRegions);
        }
    }
}
