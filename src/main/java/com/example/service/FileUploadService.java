package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileUploadService {

    public String saveFileTemporarily(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        
        // Validate if it's an Excel file
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Only Excel files (.xls, .xlsx) are allowed");
        }

        // Create a temporary file in the default temp directory
        Path tempFile = Files.createTempFile("salary_upload_", "_" + originalFilename);
        
        // Transfer the multipart file content to the temp file
        file.transferTo(tempFile.toFile());
        
        return tempFile.toAbsolutePath().toString();
    }
}
