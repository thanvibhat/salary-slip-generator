package com.example.controller;

import com.example.model.ApiResponse;
import com.example.service.FileUploadService;
import com.example.service.SalarySlipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final FileUploadService fileUploadService;
    private final SalarySlipService salarySlipService;

    public UploadController(FileUploadService fileUploadService, SalarySlipService salarySlipService) {
        this.fileUploadService = fileUploadService;
        this.salarySlipService = salarySlipService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        try {
            // 1. Save file temporarily
            String tempPath = fileUploadService.saveFileTemporarily(file);
            
            // 2. Define output directory for PDFs
            String outputDir = System.getProperty("user.dir") + File.separator + "generated_slips";
            
            // 3. Run existing pipeline via service
            List<String> generatedFiles = salarySlipService.generateSalarySlips(tempPath, outputDir);
            
            // 4. Return response with resulting PDFs
            return ResponseEntity.ok(new ApiResponse(true, "Files generated successfully.", generatedFiles));
        } catch (Exception e) {
            throw new RuntimeException("Failed to process the salary slip generation", e);
        }
    }
}
