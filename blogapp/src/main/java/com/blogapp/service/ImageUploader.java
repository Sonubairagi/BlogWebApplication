package com.blogapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ImageUploader {
    String uploadImage(MultipartFile image);

    List<String> allFiles();

    String preSignedUrl(String fileName);

    String getImageUrlByName(String fileName);
}
