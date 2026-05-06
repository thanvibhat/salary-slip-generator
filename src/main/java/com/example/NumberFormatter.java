package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting numeric salary values.
 */
public class NumberFormatter {

    /**
     * Legacy support for the main format method.
     * Delegates to formatCurrency.
     */
    /**
     * Formats a value as an amount: adds Rupees symbol (₹), locale-aware commas, and .00 suffix.
     */
    public static String formatAmount(Object value) {
        BigDecimal numericValue = toBigDecimal(value);
        if (numericValue == null) numericValue = BigDecimal.ZERO;

        // Round to nearest integer and ensure .00 as per user preference
        BigDecimal rounded = numericValue.setScale(0, RoundingMode.HALF_UP);
        
        // Use NumberInstance with Indian locale for grouping/commas
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        
        // Using "Rs. " instead of "₹" because many PDF fonts do not support the special character
        return "Rs. " + formatter.format(rounded);
    }

    /**
     * Formats a value as a decimal: adds .00 suffix but NO currency symbol.
     */
    public static String formatDecimal(Object value) {
        BigDecimal numericValue = toBigDecimal(value);
        if (numericValue == null) return "0.00";

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
        if (numericValue == null) return "0";

        // Round to nearest integer with no decimals
        return numericValue.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Standardized method for formatting Employee IDs.
     * Ensures NO decimals or scientific notation are ever present.
     */
    public static String formatID(Object value) {
        BigDecimal numericValue = toBigDecimal(value);
        if (numericValue == null) return "";

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
