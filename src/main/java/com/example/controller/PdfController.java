package com.example.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class PdfController {

    private final Path fileStorageLocation = Paths.get(System.getProperty("user.dir") + File.separator + "generated_slips").toAbsolutePath().normalize();

    @GetMapping("/pdf/{filename}")
    public ResponseEntity<Resource> viewPdf(@PathVariable("filename") String filename) {
        return serveFile(filename, "inline");
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable("filename") String filename) {
        return serveFile(filename, "attachment");
    }

    @GetMapping("/download-all")
    public ResponseEntity<StreamingResponseBody> downloadAllZipped() {
        if (!Files.exists(fileStorageLocation) || !Files.isDirectory(fileStorageLocation)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        StreamingResponseBody streamResponseBody = out -> {
            try (ZipOutputStream zos = new ZipOutputStream(out);
                 DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fileStorageLocation, "*.pdf")) {

                boolean hasEntries = false;
                for (Path file : directoryStream) {
                    hasEntries = true;
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    
                    try (InputStream fis = Files.newInputStream(file)) {
                        StreamUtils.copy(fis, zos); // Streams file chunk-by-chunk to the ZipOutputStream
                    }
                    
                    zos.closeEntry();
                }

                // A ZipOutputStream throws ZipException if closed without entries.
                // Output a friendly fallback file so the archive remains structurally intact
                if (!hasEntries) {
                    ZipEntry zipEntry = new ZipEntry("no_slips_found.txt");
                    zos.putNextEntry(zipEntry);
                    zos.write("No PDF files were found to export.".getBytes());
                    zos.closeEntry();
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"salary_slips.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(streamResponseBody);
    }

    private ResponseEntity<Resource> serveFile(String filename, String disposition) {
        // Ensure path safety strictly preventing directory traversal attacks
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            // Guard against traversal escaping the base directory
            if (!filePath.startsWith(this.fileStorageLocation)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
