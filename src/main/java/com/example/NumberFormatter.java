package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting numeric salary values.
 */
public class NumberFormatter {

    /**
     * Legacy support for the main format method.
     * Delegates to formatCurrency.
     */
    public static String format(Object value) {
        return formatCurrency(value);
    }

    /**
     * Formats a value as currency: rounds to nearest integer and adds .00 suffix.
     * Returns null only if the input is null or unparseable.
     */
    public static String formatCurrency(Object value) {
        BigDecimal numericValue = toBigDecimal(value);
        if (numericValue == null) return null;

        // Round to nearest integer and add .00
        BigDecimal rounded = numericValue.setScale(0, RoundingMode.HALF_UP);
        return rounded.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    /**
     * Formats a value as a whole integer: rounds to nearest whole number.
     * Returns null only if the input is null or unparseable.
     */
    public static String formatInteger(Object value) {
        BigDecimal numericValue = toBigDecimal(value);
        if (numericValue == null) return null;

        // Round to nearest integer with no decimals
        return numericValue.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Standardized method for formatting Employee IDs.
     * Ensures NO decimals or scientific notation are ever present.
     */
    public static String formatID(Object value) {
        BigDecimal numericValue = toBigDecimal(value);
        if (numericValue == null) return "N/A";

        // Convert to BigInteger to strip all fractional parts
        return numericValue.setScale(0, RoundingMode.DOWN).toBigInteger().toString();
    }

    /**
     * Safe conversion of object to BigDecimal.
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            } else if (value instanceof Number) {
                return new BigDecimal(value.toString());
            } else if (value instanceof String) {
                String str = ((String) value).trim();
                if (str.isEmpty()) return null;
                return new BigDecimal(str);
            }
        } catch (NumberFormatException e) {
            // Silently handle invalid numeric strings
        }
        return null;
    }
}
