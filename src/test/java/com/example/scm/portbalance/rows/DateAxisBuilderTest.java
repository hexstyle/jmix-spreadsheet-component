package com.example.scm.portbalance.rows;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class DateAxisBuilderTest {

    private final DateAxisBuilder builder = new DateAxisBuilder();

    @Test
    void buildsRowsWithMonthBreaksForRange() {
        LocalDate from = LocalDate.of(2026, 1, 30);
        LocalDate to = LocalDate.of(2026, 2, 2);

        List<PortBalanceRow> rows = builder.build(from, to);

        Assertions.assertThat(rows).hasSize(6);

        assertRow(rows.get(0), "M:2026-01", from, true, "2026-01");
        assertRow(rows.get(1), "D:2026-01-30", LocalDate.of(2026, 1, 30), false, null);
        assertRow(rows.get(2), "D:2026-01-31", LocalDate.of(2026, 1, 31), false, null);
        assertRow(rows.get(3), "M:2026-02", LocalDate.of(2026, 2, 1), true, "2026-02");
        assertRow(rows.get(4), "D:2026-02-01", LocalDate.of(2026, 2, 1), false, null);
        assertRow(rows.get(5), "D:2026-02-02", LocalDate.of(2026, 2, 2), false, null);
    }

    @Test
    void insertsMonthBreakAtRangeStart() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 3);

        List<PortBalanceRow> rows = builder.build(from, to);

        Assertions.assertThat(rows).hasSize(4);
        assertRow(rows.get(0), "M:2026-03", LocalDate.of(2026, 3, 1), true, "2026-03");
        assertRow(rows.get(1), "D:2026-03-01", LocalDate.of(2026, 3, 1), false, null);
        assertRow(rows.get(2), "D:2026-03-02", LocalDate.of(2026, 3, 2), false, null);
        assertRow(rows.get(3), "D:2026-03-03", LocalDate.of(2026, 3, 3), false, null);
    }

    private void assertRow(PortBalanceRow row,
                           String rowId,
                           LocalDate date,
                           boolean isMonthBreak,
                           String label) {
        Assertions.assertThat(row.getRowId()).isEqualTo(rowId);
        Assertions.assertThat(row.getDate()).isEqualTo(date);
        Assertions.assertThat(row.isMonthBreak()).isEqualTo(isMonthBreak);
        Assertions.assertThat(row.getLabel()).isEqualTo(label);
    }
}
