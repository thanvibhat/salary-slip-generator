package com.example;

import net.sf.jasperreports.engine.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating PDF salary slips using JasperReports.
 */
public class PDFGenerator {

    /**
     * Generates a PDF file from a data map and a JRXML template.
     */
    public void generatePDF(Map<String, Object> data, String outputPath) throws JRException {
        // Ensure output directory exists
        File outputFile = new File(outputPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        // Load and compile template
        InputStream templateStream = getClass().getClassLoader().getResourceAsStream("salary_slip.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

        // Prepare parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.putAll(data);
        // 2. Add special/dynamic parameters
        String monthLabel = String.valueOf(data.getOrDefault("salary_month", "February 2025"));
        parameters.put("salary_month", monthLabel);
        parameters.put("logo_path", getClass().getClassLoader().getResource("download.png").toString());

        System.out.println("Standardized Parameters: " + parameters);

        // Fill and Export
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

        System.out.println("    [SUCCESS] Generated PDF: " + outputPath);
    }

    /**
     * Generates a safe and professional filename using the employee name and ID.
     * Replaces spaces with underscores and ensures ID has no decimals.
     * Example: "Abdul_Hadi_Bin_Nizar_1067.pdf"
     */
    public static String generateFileName(Map<String, Object> data) {
        String name = String.valueOf(data.getOrDefault("Employee Name", "Unknown")).trim();
        Object rawId = data.getOrDefault("Employee ID", "N/A");

        // 1. Format Name: Replace spaces with underscores
        String safeName = name.replace(" ", "_").replaceAll("[^a-zA-Z0-9._-]", "");

        // 2. Format ID: Use standardized formatting (removes decimals)
        String safeId = NumberFormatter.formatID(rawId);

        return safeName + "_" + safeId + ".pdf";
    }
}
