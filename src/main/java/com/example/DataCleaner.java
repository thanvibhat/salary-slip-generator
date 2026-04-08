package com.example;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Advanced Data Cleaner that standardizes keys and formats salary values using Maps.
 */
public class DataCleaner {

    // Centralized mapping from Excel Headers to JRXML Parameters
    private static final Map<String, String> FIELD_MAPPING = new LinkedHashMap<>();

    static {
        // Employee Info
        FIELD_MAPPING.put("Employee ID", "Employee ID");
        FIELD_MAPPING.put("Employee Name", "Employee Name");
        FIELD_MAPPING.put("Gender", "Gender");
        FIELD_MAPPING.put("UAN", "UAN");
        FIELD_MAPPING.put("Bank Name", "Bank Name");
        FIELD_MAPPING.put("Bank Account #", "Bank Account #");
        FIELD_MAPPING.put("Bank IFSC Code", "Bank IFSC Code");
        FIELD_MAPPING.put("City", "City");

        // Attendance & Leaves
        FIELD_MAPPING.put("Days in month", "Days in month");
        FIELD_MAPPING.put("Days of Pay", "Days of Pay");
        FIELD_MAPPING.put("Opening Leave Balance", "Opening Leave Balance");
        FIELD_MAPPING.put("Eligible Leaves", "Eligible Leaves");
        FIELD_MAPPING.put("Leaves Availed", "Leaves Availed");
        FIELD_MAPPING.put("Loss of Pay Days", "Loss of Pay Days");
        FIELD_MAPPING.put("Closing Leave Balance", "Closing Leave Balance");
        FIELD_MAPPING.put("Payable Days", "Payable Days");

        // Additions
        FIELD_MAPPING.put("Payable Basic + DA", "Payable Basic + DA");
        FIELD_MAPPING.put("Payable HRA", "Payable HRA");
        FIELD_MAPPING.put("Payable Medical Allowance", "Payable Medical Allowance");
        FIELD_MAPPING.put("Payable Conveyance", "Payable Conveyance");
        FIELD_MAPPING.put("Individual Performance Incentive", "Individual Performance Incentive");
        FIELD_MAPPING.put("Company Performance Incentive", "Company Performance Incentive");
        FIELD_MAPPING.put("Other Arrears / Incentives", "Other Arrears / Incentives");
        FIELD_MAPPING.put("Bonus Accrued", "Bonus Accrued");
        FIELD_MAPPING.put("Total Additions", "Total Additions");

        // Deductions
        FIELD_MAPPING.put("Provident Fund (PF)", "Provident Fund (PF)");
        FIELD_MAPPING.put("Professional Tax (PT)", "Professional Tax (PT)");
        FIELD_MAPPING.put("Employees' State Insurance (ESI)", "Employees' State Insurance (ESI)");
        FIELD_MAPPING.put("Loan EMI", "Loan EMI");
        FIELD_MAPPING.put("Total Deductions", "Total Deductions");

        // Final Totals
        FIELD_MAPPING.put("Payable Salary", "Payable Salary");
        FIELD_MAPPING.put("Annual Bonus", "Annual Bonus");
        FIELD_MAPPING.put("Closing Loan Balance", "Closing Loan Balance");
    }

    /**
     * Cleans raw Map data from Excel:
     * 1. Maps keys to JRXML parameter names.
     * 2. Formats numeric/salary values with .00 suffix via NumberFormatter.
     * 3. Excludes null, empty, or zero values.
     */
    public Map<String, Object> clean(Map<String, Object> rawData) {
        if (rawData == null) return null;

        Map<String, Object> cleanedData = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            String excelKey = entry.getKey();
            String jrxmlKey = entry.getValue();

            Object rawValue = findRawValue(rawData, excelKey);
            if (rawValue == null) continue;

            if (isSalaryField(jrxmlKey) || isNumeric(rawValue)) {
                String formatted = NumberFormatter.format(rawValue);
                if (formatted != null) {
                    cleanedData.put(jrxmlKey, formatted);
                }
            } else {
                String strValue = String.valueOf(rawValue).trim();
                if (!strValue.isEmpty() && !"null".equalsIgnoreCase(strValue)) {
                    cleanedData.put(jrxmlKey, strValue);
                }
            }
        }
        return cleanedData;
    }

    private Object findRawValue(Map<String, Object> data, String targetKey) {
        for (String key : data.keySet()) {
            if (key.trim().equalsIgnoreCase(targetKey.trim())) {
                return data.get(key);
            }
        }
        return null;
    }

    private boolean isSalaryField(String jrxmlKey) {
        return jrxmlKey.contains("Payable") || jrxmlKey.contains("Total") || 
               jrxmlKey.contains("Incentive") || jrxmlKey.contains("Bonus") ||
               jrxmlKey.contains("Fund") || jrxmlKey.contains("Tax") || 
               jrxmlKey.contains("Insurance") || jrxmlKey.contains("EMI") ||
               jrxmlKey.contains("Balance") && jrxmlKey.contains("Loan");
    }

    private boolean isNumeric(Object value) {
        if (value instanceof Number) return true;
        if (value instanceof String) {
            return ((String) value).trim().matches("-?\\d+(\\.\\d+)?");
        }
        return false;
    }
}
