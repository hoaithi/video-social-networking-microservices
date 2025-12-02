//package com.hoaithi.file_service.controller;
//
//import com.hoaithi.file_service.service.FileStorageService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//public class FileController {
//
//    private final FileStorageService fileStorageService;
//    public FileController(FileStorageService fileStorageService) {
//        this.fileStorageService = fileStorageService;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        try {
//            String key = fileStorageService.uploadFile(file);
//            String url = fileStorageService.generatePresignedUrl(key);
//            return ResponseEntity.ok(url);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
//        }
//    }
//}




package com.hoaithi.file_service.controller;

import com.hoaithi.file_service.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload file endpoint
     * Tự động xử lý cả file nhỏ và file lớn (video)
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("=== Upload Request Received ===");
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {} bytes ({} MB)", file.getSize(), file.getSize() / (1024.0 * 1024.0));

        try {
            if (file.isEmpty()) {
                log.warn("Upload failed: Empty file");
                return ResponseEntity.badRequest().body("File is empty");
            }

            long startTime = System.currentTimeMillis();
            String key = fileStorageService.uploadFile(file);
            long uploadTime = System.currentTimeMillis() - startTime;

            String url = fileStorageService.generatePresignedUrl(key);

            log.info("=== Upload Completed Successfully ===");
            log.info("Key: {}", key);
            log.info("URL: {}", url);
            log.info("Upload time: {} ms", uploadTime);

            // ✅ CHỈ TRẢ VỀ URL
            return ResponseEntity.ok(url);

        } catch (Exception e) {
            log.error("=== Upload Failed ===", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Endpoint để trả về URL từ key (nếu cần)
     */
    @GetMapping("/url/{key}")
    public ResponseEntity<?> getFileUrl(@PathVariable String key) {
        log.info("Generating URL for key: {}", key);

        try {
            if (!fileStorageService.fileExists(key)) {
                log.warn("File not found: {}", key);
                return ResponseEntity.notFound().build();
            }

            String url = fileStorageService.generatePresignedUrl(key);

            Map<String, String> response = new HashMap<>();
            response.put("key", key);
            response.put("url", url);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating URL for key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate URL: " + e.getMessage());
        }
    }

    /**
     * Endpoint để xóa file
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteFile(@PathVariable String key) {
        log.info("Delete request for key: {}", key);

        try {
            if (!fileStorageService.fileExists(key)) {
                log.warn("File not found: {}", key);
                return ResponseEntity.notFound().build();
            }

            fileStorageService.deleteFile(key);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File deleted successfully");
            response.put("key", key);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting file: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file: " + e.getMessage());
        }
    }
}

