package com.blogapp.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.blogapp.exception.ImageUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class S3ImagerUploader implements ImageUploader{

    @Autowired
    private AmazonS3 client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile image) {
        if(image.isEmpty()){
            throw new ImageUploadException("Image is null !!");
        }

        try{
            //we can find the image path
            String originalFilePath = image.getOriginalFilename();

            //convert to main file path to modify fileName
            String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));

            //object mate data of image ->
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.getSize());
//            metadata.setContentType("images");

            //make the amazon client object to put the file
            PutObjectResult putObjectResult = client.putObject(new PutObjectRequest(
                    bucketName,
                    fileName,
                    image.getInputStream(),
                    metadata
            ));
//            System.out.println("hello");
            return this.preSignedUrl(fileName);

            //handle the exception user define and any other exception
        }catch (IOException e){
            throw new ImageUploadException("Error in uploading image :- "+ e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> allFiles() {
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName);

        ListObjectsV2Result listObjectsV2Result = client.listObjectsV2(listObjectsRequest);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        List<String> lisFileUrls = objectSummaries.stream().map(item -> this.preSignedUrl(item.getKey())).collect(Collectors.toList());
        return lisFileUrls;
    }

    @Override
    public String preSignedUrl(String fileName) {
        Date expirationDate = new Date();

        Long time = expirationDate.getTime();
        int hour = 2;
        time = time + hour * 60 * 60;

        expirationDate.setTime(time);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(
                        bucketName,
                        fileName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expirationDate);
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    @Override
    public String getImageUrlByName(String fileName) {
        S3Object object = client.getObject(bucketName, fileName);
        String key = object.getKey();
        String url = preSignedUrl(key);
        return url;
    }
}
