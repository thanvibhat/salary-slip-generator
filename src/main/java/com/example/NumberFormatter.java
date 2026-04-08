package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting numeric salary values.
 */
public class NumberFormatter {

    /**
     * Formats a salary value: rounds to nearest integer (HALF_UP) and adds .00 suffix.
     * Returns null if the value is null, empty, or zero (to be excluded).
     *
     * @param value The value to format (can be Number, String, or null)
     * @return Formatted string with .00 suffix, or null if should be excluded.
     */
    public static String format(Object value) {
        if (value == null) {
            return null;
        }

        BigDecimal numericValue;

        try {
            if (value instanceof BigDecimal) {
                numericValue = (BigDecimal) value;
            } else if (value instanceof Number) {
                numericValue = new BigDecimal(value.toString());
            } else if (value instanceof String) {
                String str = ((String) value).trim();
                if (str.isEmpty()) {
                    return null;
                }
                numericValue = new BigDecimal(str);
            } else {
                return null; // Invalid type
            }
        } catch (NumberFormatException e) {
            return null; // Invalid numeric string
        }

        // Check if value is zero
        if (numericValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // 1. Round to nearest integer using HALF_UP
        BigDecimal rounded = numericValue.setScale(0, RoundingMode.HALF_UP);

        // 2. Display with .00 suffix
        // We set scale to 2 to get the .00 suffix. 
        // Since we already rounded to 0 decimals, this will always be .00
        BigDecimal result = rounded.setScale(2, RoundingMode.UNNECESSARY);

        return result.toPlainString();
    }
}
