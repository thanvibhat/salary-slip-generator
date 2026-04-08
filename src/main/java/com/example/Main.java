package com.example;

import java.util.List;
import java.util.Map;

/**
 * Main Application Orchestrator for the Salary Slip Generator.
 * 
 * This class coordinates the end-to-end data pipeline:
 * 1. Extraction: Reads raw data from the Excel source.
 * 2. Transformation: Cleans, maps, and formats data for the report template.
 * 3. Generation: Produces the final PDF salary slips via JasperReports.
 */
public class Main {

    // Configuration Constants
    private static final String INPUT_EXCEL_PATH = "input/Salary Statement - 2025.xlsx";
    private static final String OUTPUT_DIRECTORY = "output/";

    public static void main(String[] args) {
        // Support any Excel file name provided as a command-line argument
        String excelPath = (args.length > 0) ? args[0] : INPUT_EXCEL_PATH;

        System.out.println("=================================================");
        System.out.println("       SALARY SLIP GENERATION PIPELINE           ");
        System.out.println("=================================================");

        try {
            // Initialize Core Components
            ExcelReader excelReader = new ExcelReader();
            DataCleaner dataCleaner = new DataCleaner();
            PDFGenerator pdfGenerator = new PDFGenerator();

            // STEP 1: Extract Data
            System.out.println("[DEBUG] Using Input File: " + excelPath);
            System.out.println("[INFO] Reading source data from: " + excelPath);
            List<Map<String, Object>> employeeRecords = excelReader.readExcel(excelPath);
            System.out.println("[INFO] Successfully extracted " + employeeRecords.size() + " records.\n");

            // STEP 2 & 3: Transform and Generate
            int successCount = 0;
            int failureCount = 0;

            for (Map<String, Object> rawData : employeeRecords) {
                // Get identifiers for logging
                String name = String.valueOf(rawData.getOrDefault("Employee Name", "Unknown"));
                String id = String.valueOf(rawData.getOrDefault("Employee ID", "N/A"));

                try {
                    System.out.println(">>> Processing Employee: " + name + " (ID: " + id + ")");

                    // Transformation Logic (Cleaning, Mapping, Formatting)
                    Map<String, Object> cleanedData = dataCleaner.clean(rawData);

                    if (cleanedData == null || cleanedData.isEmpty()) {
                        System.err.println("    [WARNING] Skipping record: Cleaning resulted in empty data.");
                        failureCount++;
                        continue;
                    }

                    // 2. Determine Output File Name
                    String fileName;
                    if (cleanedData.containsKey("Employee Name")) {
                        String cleanName = String.valueOf(cleanedData.get("Employee Name"));
                        // Strip invalid filename characters
                        cleanName = cleanName.replaceAll("[^a-zA-Z0-9._-]", "_");
                        fileName = cleanName + "_" + id + ".pdf";
                    } else {
                        fileName = "salary_slip_" + id + ".pdf";
                    }

                    String fullOutputPath = OUTPUT_DIRECTORY + fileName;

                    // 3. PDF Generation Logic
                    pdfGenerator.generatePDF(cleanedData, fullOutputPath);
                    
                    System.out.println("    [SUCCESS] PDF generated successfully.");
                    successCount++;

                } catch (Exception e) {
                    System.err.println("    [ERROR] Failed to process record: " + e.getMessage());
                    failureCount++;
                    // Continue to next employee record
                }
            }

            // Final Summary
            System.out.println("\n=================================================");
            System.out.println("               PROCESSING SUMMARY                ");
            System.out.println("=================================================");
            System.out.println(" Total Records Processed : " + employeeRecords.size());
            System.out.println(" Successfully Generated  : " + successCount);
            System.out.println(" Skipped or Failed      : " + failureCount);
            System.out.println(" Output Folder           : " + OUTPUT_DIRECTORY);
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("[FATAL] Pipeline stopped due to unexpected error.");
            System.err.println("[REASON] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
