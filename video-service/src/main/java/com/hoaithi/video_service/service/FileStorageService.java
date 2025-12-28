//package com.hoaithi.file_service.service;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//public class FileStorageService {
//
//    private final S3Client s3Client;
//    public FileStorageService(S3Client s3Client) {
//        this.s3Client = s3Client;
//    }
//
//    @Value("${aws.s3.bucket-name}")
//    private String bucketName;
//
//    public String uploadFile(MultipartFile file) throws IOException {
//        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//        s3Client.putObject(
//                PutObjectRequest.builder()
//                        .bucket(bucketName)
//                        .key(key)
//                        .contentType(file.getContentType())
//                        .build(),
//                RequestBody.fromBytes(file.getBytes())
//        );
//
//        return key; // bạn lưu key này trong DB hoặc trả ra client
//    }
//
//    public String generatePresignedUrl(String key) {
//        return s3Client.utilities()
//                .getUrl(GetUrlRequest.builder()
//                        .bucket(bucketName)
//                        .key(key)
//                        .build())
//                .toString();
//    }
//}










package com.hoaithi.video_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final S3Client s3Client;

    // Kích thước mỗi part cho multipart upload (10MB)
    private static final long PART_SIZE = 10 * 1024 * 1024; // 10MB
    // Ngưỡng để sử dụng multipart upload (20MB)
    private static final long MULTIPART_THRESHOLD = 20 * 1024 * 1024; // 20MB

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Upload file với tự động chọn phương thức phù hợp
     * - File nhỏ (<20MB): Simple upload
     * - File lớn (>=20MB): Multipart upload với streaming
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        long fileSize = file.getSize();

        log.info("Starting upload for file: {} - Size: {} bytes ({} MB)",
                file.getOriginalFilename(), fileSize, fileSize / (1024.0 * 1024.0));

        if (fileSize >= MULTIPART_THRESHOLD) {
            log.info("File size exceeds threshold, using multipart upload");
            uploadLargeFile(file, key);
        } else {
            log.info("Using simple upload for small file");
            uploadSmallFile(file, key);
        }

        log.info("Upload completed successfully for key: {}", key);
        return key;
    }

    /**
     * Upload file nhỏ trực tiếp
     */
    private void uploadSmallFile(MultipartFile file, String key) throws IOException {
        log.debug("Uploading small file to S3: {}", key);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        log.debug("Small file upload completed: {}", key);
    }

    /**
     * Upload file lớn sử dụng multipart upload với streaming
     * Tối ưu cho video và file lớn, không load toàn bộ vào memory
     */
    private void uploadLargeFile(MultipartFile file, String key) throws IOException {
        log.info("Initiating multipart upload for: {}", key);

        // Bước 1: Khởi tạo multipart upload
        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
        String uploadId = createResponse.uploadId();

        log.info("Multipart upload initiated - Upload ID: {}", uploadId);

        List<CompletedPart> completedParts = new ArrayList<>();
        int partNumber = 1;
        long totalBytesRead = 0;

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[(int) PART_SIZE];
            int bytesRead;

            // Bước 2: Upload từng part với streaming
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                log.debug("Uploading part {} - Size: {} bytes", partNumber, bytesRead);

                // Tạo byte array với đúng kích thước đã đọc
                byte[] partData = new byte[bytesRead];
                System.arraycopy(buffer, 0, partData, 0, bytesRead);

                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength((long) bytesRead)
                        .build();

                UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                        uploadPartRequest,
                        RequestBody.fromBytes(partData)
                );

                CompletedPart part = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(uploadPartResponse.eTag())
                        .build();

                completedParts.add(part);
                totalBytesRead += bytesRead;

                log.info("Part {} uploaded successfully - ETag: {} - Progress: {}/{} bytes ({:.2f}%)",
                        partNumber, uploadPartResponse.eTag(),
                        totalBytesRead, file.getSize(),
                        (totalBytesRead * 100.0) / file.getSize());

                partNumber++;
            }

            // Bước 3: Hoàn thành multipart upload
            log.info("Completing multipart upload with {} parts", completedParts.size());

            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build();

            s3Client.completeMultipartUpload(completeRequest);

            log.info("Multipart upload completed successfully - Total parts: {}, Total size: {} bytes",
                    completedParts.size(), totalBytesRead);

        } catch (Exception e) {
            log.error("Error during multipart upload, aborting upload ID: {}", uploadId, e);

            // Abort multipart upload nếu có lỗi
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .build();

            s3Client.abortMultipartUpload(abortRequest);
            log.warn("Multipart upload aborted: {}", uploadId);

            throw new IOException("Failed to upload large file: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo presigned URL để truy cập file
     */
    public String generatePresignedUrl(String key) {
        log.debug("Generating presigned URL for key: {}", key);

        String url = s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .toString();

        log.debug("Presigned URL generated: {}", url);
        return url;
    }

    /**
     * Xóa file từ S3
     */
    public void deleteFile(String key) {
        log.info("Deleting file from S3: {}", key);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);

        log.info("File deleted successfully: {}", key);
    }

    /**
     * Kiểm tra file có tồn tại không
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            log.debug("File exists: {}", key);
            return true;
        } catch (NoSuchKeyException e) {
            log.debug("File does not exist: {}", key);
            return false;
        }
    }
}