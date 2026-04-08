package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to read employee salary data from Excel files.
 */
public class ExcelReader {

    /**
     * Reads the Excel file and returns a list of employee data maps.
     * Includes basic validation for file existence and sheet selection.
     */
    public List<Map<String, Object>> readExcel(String filePath) throws IOException {
        File excelFile = new File(filePath);
        
        // 1. Validate file existence and type
        if (!excelFile.exists()) {
            throw new FileNotFoundException("The specified Excel file does not exist: " + excelFile.getAbsolutePath());
        }
        if (!excelFile.isFile()) {
            throw new IOException("The specified path is not a file: " + excelFile.getAbsolutePath());
        }

        List<Map<String, Object>> employeeList = new ArrayList<>();

        // 2. Open Workbook using try-with-resources
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            // 3. Select Sheet (Fallback to active sheet if 'Generic' is missing)
            Sheet sheet = workbook.getSheet("Generic");
            if (sheet == null) {
                int activeIndex = workbook.getActiveSheetIndex();
                sheet = (activeIndex >= 0) ? workbook.getSheetAt(activeIndex) : workbook.getSheetAt(0);
            }
            
            if (sheet == null) {
                throw new RuntimeException("No valid sheet found in the Excel file!");
            }

            // 4. Extract Salary Month from Cell B1 (Row 0, Cell 1)
            String salaryMonth = "Unknown";
            Row firstRow = sheet.getRow(0);
            if (firstRow != null) {
                Cell monthCell = firstRow.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Object val = getCellValue(monthCell);
                salaryMonth = DateFormatter.formatMonthYear(val);
            }

            // 5. Extract Headers from Row 2 (index 1)
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) {
                return employeeList;
            }

            List<String> headers = new ArrayList<>();
            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                Cell cell = headerRow.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                    String header = cell.getStringCellValue().trim();
                    headers.add(header.isEmpty() ? "Col_" + j : header.replaceAll("\\s+", " "));
                } else {
                    headers.add("Col_" + j);
                }
            }

            // 6. Read Data Rows (starting from index 2)
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row currentRow = sheet.getRow(i);
                if (currentRow == null) continue;

                Map<String, Object> employeeData = new LinkedHashMap<>();
                employeeData.put("salary_month", salaryMonth); // Include global salary month
                for (int j = 0; j < headers.size(); j++) {
                    String columnName = headers.get(j);
                    Cell cell = currentRow.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    employeeData.put(columnName, getCellValue(cell));
                }
                employeeList.add(employeeData);
            }
        }

        return employeeList;
    }

    /**
     * Helper method to safely extract values from cells based on their type.
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DateFormatter.formatMonthYear(cell.getLocalDateTimeCellValue().toLocalDate());
                }
                double value = cell.getNumericCellValue();
                return (value == Math.floor(value)) ? (long) value : value;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try { 
                    return cell.getNumericCellValue(); 
                } catch (Exception e) { 
                    return cell.getStringCellValue(); 
                }
            default:
                return null;
        }
    }
}
