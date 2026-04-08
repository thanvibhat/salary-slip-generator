package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Utility class for safe and robust date formatting.
 * Handles LocalDate objects and various string date formats.
 */
public class DateFormatter {

    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Formats a date value to "Month Year" (e.g., February 2026).
     * @param value The object to format (can be LocalDate or String)
     * @return Formatted string or the original value if unparseable.
     */
    public static String formatMonthYear(Object value) {
        if (value == null) return "Unknown";

        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(MONTH_YEAR_FORMATTER);
        }

        String strVal = String.valueOf(value).trim();
        if (strVal.isEmpty()) return "Unknown";

        try {
            // Attempt to parse standard ISO YYYY-MM-DD
            LocalDate date = LocalDate.parse(strVal);
            return date.format(MONTH_YEAR_FORMATTER);
        } catch (DateTimeParseException e) {
            // If parsing fails, return the string as-is to avoid data loss
            return strVal;
        }
    }
}
