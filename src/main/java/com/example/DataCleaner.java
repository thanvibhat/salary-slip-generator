package com.example;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Advanced Data Cleaner that applies section-based filtering rules:
 * 1. Employee Details: Always included.
 * 2. Additions/Deductions/Totals: Included only if > 0.
 */
public class DataCleaner {

    private enum Section {
        EMPLOYEE_INFO,
        ADDITIONS,
        DEDUCTIONS,
        TOTALS,
        QUOTAS
    }

    private static final Map<String, String> FIELD_MAPPING = new LinkedHashMap<>();
    private static final Map<String, Section> FIELD_SECTIONS = new LinkedHashMap<>();

    static {
        // --- EMPLOYEE INFO (Always Include) ---
        addMapping("Employee ID", "Employee ID", Section.EMPLOYEE_INFO);
        addMapping("Employee Name", "Employee Name", Section.EMPLOYEE_INFO);
        addMapping("Gender", "Gender", Section.EMPLOYEE_INFO);
        addMapping("UAN", "UAN", Section.EMPLOYEE_INFO);
        addMapping("Bank Name", "Bank Name", Section.EMPLOYEE_INFO);
        addMapping("Bank Account #", "Bank Account #", Section.EMPLOYEE_INFO);
        addMapping("Bank IFSC Code", "Bank IFSC Code", Section.EMPLOYEE_INFO);
        addMapping("City", "City", Section.EMPLOYEE_INFO);
        addMapping("Days in month", "Days in month", Section.EMPLOYEE_INFO);
        addMapping("Days of Pay", "Days of Pay", Section.EMPLOYEE_INFO);
        addMapping("Payable Days", "Payable Days", Section.EMPLOYEE_INFO);
        addMapping("salary_month", "salary_month", Section.EMPLOYEE_INFO);

        // --- ADDITIONS (Include if > 0) ---
        addMapping("Payable Basic + DA", "Payable Basic + DA", Section.ADDITIONS);
        addMapping("Payable HRA", "Payable HRA", Section.ADDITIONS);
        addMapping("Payable Medical Allowance", "Payable Medical Allowance", Section.ADDITIONS);
        addMapping("Payable Conveyance", "Payable Conveyance", Section.ADDITIONS);
        addMapping("Individual Performance Incentive", "Individual Performance Incentive", Section.ADDITIONS);
        addMapping("Company Performance Incentive", "Company Performance Incentive", Section.ADDITIONS);
        addMapping("Other Arrears / Incentives", "Other Arrears / Incentives", Section.ADDITIONS);
        addMapping("Bonus Accrued", "Bonus Accrued", Section.ADDITIONS);
        addMapping("Total Additions", "Total Additions", Section.ADDITIONS);
        addMapping("Annual Bonus", "Annual Bonus", Section.ADDITIONS);

        // --- DEDUCTIONS (Include if > 0) ---
        addMapping("Provident Fund (PF)", "Provident Fund (PF)", Section.DEDUCTIONS);
        addMapping("Professional Tax (PT)", "Professional Tax (PT)", Section.DEDUCTIONS);
        addMapping("Employees' State Insurance (ESI)", "Employees' State Insurance (ESI)", Section.DEDUCTIONS);
        addMapping("Tax Deduction on Source (TDS)", "Tax Deduction on Source (TDS)", Section.DEDUCTIONS);
        addMapping("Loan EMI", "Loan EMI", Section.DEDUCTIONS);
        addMapping("Total Deductions", "Total Deductions", Section.DEDUCTIONS);

        // --- TOTALS & BALANCES (Include if > 0) ---
        addMapping("Payable Salary", "Payable Salary", Section.TOTALS);
        addMapping("Closing Loan Balance", "Closing Loan Balance", Section.TOTALS);
        addMapping("Opening Leave Balance", "Opening Leave Balance", Section.QUOTAS);
        addMapping("Eligible Leaves", "Eligible Leaves", Section.QUOTAS);
        addMapping("Leaves Availed", "Leaves Availed", Section.QUOTAS);
        addMapping("Loss of Pay Days", "Loss of Pay Days", Section.QUOTAS);
        addMapping("Closing Leave Balance", "Closing Leave Balance", Section.QUOTAS);
    }

    private static void addMapping(String excel, String jrxml, Section section) {
        FIELD_MAPPING.put(excel, jrxml);
        FIELD_SECTIONS.put(jrxml, section);
    }

    public Map<String, Object> clean(Map<String, Object> rawData) {
        if (rawData == null) return null;

        Map<String, Object> cleanedData = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            String excelKey = entry.getKey();
            String jrxmlKey = entry.getValue();
            Section section = FIELD_SECTIONS.get(jrxmlKey);

            Object rawValue = findRawValue(rawData, excelKey);
            
            // Rule: Always include Employee Info and Leave Quotas to ensure nulls show as 0.00
            boolean isDetailSection = (section == Section.EMPLOYEE_INFO || section == Section.QUOTAS);
            
            if (rawValue == null && !isDetailSection) {
                continue;
            }

            // 1. Evaluate numeric value for filtering
            BigDecimal numericValue = NumberFormatter.toBigDecimal(rawValue);
            boolean isGreaterThanZero = (numericValue != null && numericValue.compareTo(BigDecimal.ZERO) > 0);

            // 2. Apply Section-Based Filtering Rule
            boolean shouldInclude = isDetailSection || isGreaterThanZero;

            if (shouldInclude) {
                // 3. Apply Formatting
                String key = jrxmlKey.toLowerCase();
                
                if (key.contains("salary_month")) {
                    cleanedData.put(jrxmlKey, DateFormatter.formatMonthYear(rawValue));
                } else if (key.equals("employee id")) {
                    // ID remains as integer (no .00)
                    cleanedData.put(jrxmlKey, NumberFormatter.formatID(rawValue));
                } else if (key.equals("bank account #")) {
                    // Account # remains as integer (no .00)
                    cleanedData.put(jrxmlKey, NumberFormatter.formatInteger(rawValue));
                } else if (isCurrencyField(jrxmlKey)) {
                    // Add Rupees symbol, commas, and .00
                    cleanedData.put(jrxmlKey, NumberFormatter.formatAmount(rawValue));
                } else if (section == Section.QUOTAS || key.contains("days") || key.contains("uan")) {
                    // Add .00 suffix but no currency symbol
                    cleanedData.put(jrxmlKey, NumberFormatter.formatDecimal(rawValue));
                } else if (isIntegerField(jrxmlKey) || numericValue != null) {
                    cleanedData.put(jrxmlKey, NumberFormatter.formatInteger(rawValue));
                } else {
                    cleanedData.put(jrxmlKey, String.valueOf(rawValue).trim());
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

    private boolean isCurrencyField(String jrxmlKey) {
        String key = jrxmlKey.toLowerCase();
        if (key.contains("days")) return false;
        return key.contains("payable") || key.contains("total") || 
               key.contains("tax") || key.contains("basic") || 
               key.contains("allowance") || key.contains("incentive") || 
               key.contains("bonus") || key.contains("fund") || 
               key.contains("insurance") || key.contains("emi") ||
               (key.contains("balance") && key.contains("loan")) ||
               key.contains("hra") || key.contains("medical") || 
               key.contains("conveyance") || key.contains("arrears") ||
               key.contains("da ") || key.contains(" da") || key.equals("da") || // Match DA as word/suffix
               key.contains("ctc");
    }

    private boolean isIntegerField(String jrxmlKey) {
        String key = jrxmlKey.toLowerCase();
        return key.contains("id") || key.contains("account") || 
               key.contains("uan") || key.contains("days") || 
               key.contains("leave") || (key.contains("balance") && !key.contains("loan"));
    }
}
