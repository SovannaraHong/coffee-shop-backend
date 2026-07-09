package com.coffee_shop.coffee_shop.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {
    String uploadFile(MultipartFile file, String folder) throws IOException;

    void deleteFile(String fileUrl);
}