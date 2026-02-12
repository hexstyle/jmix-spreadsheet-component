package com.example.scm.portbalance.rows;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class DateAxisBuilder {

    public List<PortBalanceRow> build(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }

        List<PortBalanceRow> rows = new ArrayList<>();
        YearMonth currentMonth = null;
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            YearMonth month = YearMonth.from(cursor);
            if (!month.equals(currentMonth)) {
                rows.add(PortBalanceRow.monthBreak(cursor, month.toString()));
                currentMonth = month;
            }
            rows.add(PortBalanceRow.dateRow(cursor));
            cursor = cursor.plusDays(1);
        }

        return rows;
    }
}
