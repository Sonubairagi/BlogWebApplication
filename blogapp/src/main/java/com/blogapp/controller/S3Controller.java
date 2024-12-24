package com.blogapp.controller;

import com.blogapp.service.ImageUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import java.awt.*;

@RestController
@RequestMapping("/api/v1/s3")
public class S3Controller {

    //upload image
    @Autowired
    private ImageUploader imageUploader;

    //http://localhost:8080/api/v1/s3/uploadFile
    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file
    ){
        String fileName = imageUploader.uploadImage(file);
        return new ResponseEntity<>(fileName, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/s3
    @GetMapping
    public ResponseEntity<List<String>> getListOfFiles(){
        List<String> allFiles = imageUploader.allFiles();
        return new ResponseEntity<>(allFiles, HttpStatus.OK);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<String> getFileName(@PathVariable  String fileName){
        String imageUrlByName = imageUploader.getImageUrlByName(fileName);
        return new ResponseEntity<>(imageUrlByName, HttpStatus.OK);
    }
}
