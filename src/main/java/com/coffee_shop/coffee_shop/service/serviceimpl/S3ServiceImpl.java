package com.coffee_shop.coffee_shop.service.impl;

import com.coffee_shop.coffee_shop.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private String bucketUrlPrefix() {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (S3Exception e) {
            log.error("S3 upload failed for key {}", key, e);
            throw new IOException("Failed to upload file to S3", e);
        }

        return bucketUrlPrefix() + key;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!isManagedUrl(fileUrl)) {
            log.warn("Refusing to delete URL not managed by this bucket: {}", fileUrl);
            return;
        }

        String key = extractKey(fileUrl);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            log.error("Failed to delete S3 object with key {}", key, e);
            throw e;
        }
    }

    @Override
    public boolean isManagedUrl(String fileUrl) {
        return fileUrl != null && fileUrl.startsWith(bucketUrlPrefix());
    }

    private String extractKey(String fileUrl) {
        return fileUrl.substring(bucketUrlPrefix().length());
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}