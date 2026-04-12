package com.example.service;

import com.example.DataCleaner;
import com.example.ExcelReader;
import com.example.PDFGenerator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SalarySlipService {

    public List<String> generateSalarySlips(String excelFilePath, String outputDirectory) throws Exception {
        List<String> generatedFiles = new ArrayList<>();
        
        ExcelReader excelReader = new ExcelReader();
        DataCleaner dataCleaner = new DataCleaner();
        PDFGenerator pdfGenerator = new PDFGenerator();
        
        List<Map<String, Object>> employeeList = excelReader.readExcel(excelFilePath);
        
        for (Map<String, Object> rawData : employeeList) {
            Map<String, Object> cleanedData = dataCleaner.clean(rawData);
            
            if (cleanedData != null && !cleanedData.isEmpty()) {
                String fileName = PDFGenerator.generateFileName(cleanedData);
                String outputPath = outputDirectory + File.separator + fileName;
                
                pdfGenerator.generatePDF(cleanedData, outputPath);
                generatedFiles.add(fileName);
            }
        }
        
        return generatedFiles;
    }
}
