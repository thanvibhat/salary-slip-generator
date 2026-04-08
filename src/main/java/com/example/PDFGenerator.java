package com.example;

import net.sf.jasperreports.engine.*;

import java.io.File;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PDFGenerator {

    public void generatePDF(Map<String, Object> data, String outputPath) {
        try {
            // Load the JRXML template
            InputStream templateStream = getClass().getClassLoader().getResourceAsStream("salary_slip.jrxml");

            // Compile the template into a JasperReport object
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            // Create new map for properly formatted parameters
            Map<String, Object> parameters = new HashMap<>();

            // 1. Add standardized keys from cleaned data
            parameters.putAll(data);

            // 2. Add special/dynamic parameters
            parameters.put("month", "February 2025");
            parameters.put("logo_path", getClass().getClassLoader().getResource("download.png").toString());

            System.out.println("Standardized Parameters: " + parameters);

            // Check for missing parameters expected by JRXML
            for (JRParameter param : jasperReport.getParameters()) {
                if (!param.isSystemDefined() && !parameters.containsKey(param.getName())) {
                    System.err.println("WARNING: JRXML expects parameter '" + param.getName()
                            + "' but it is missing in Excel data!");
                }
            }

            // Fill the report with parameters and JREmptyDataSource
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));

            // Ensure output directory exists before saving
            File outputFile = new File(outputPath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            // Export the JasperPrint object directly to a PDF file
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            System.out.println("Generated PDF successfully: " + outputPath);

        } catch (Exception e) {
            System.err.println("Failed to generate PDF for " + outputPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
