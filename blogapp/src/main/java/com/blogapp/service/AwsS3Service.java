package com.blogapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.blogapp.exception.ImageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
public class AwsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3Service.class);

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // Upload Image
    public String uploadImage(MultipartFile profileImage){
        String originalFileName;
        String fileName = null;
        ObjectMetadata metadata;
        URL url;

        try{
            if (profileImage.isEmpty()){
                logger.warn("Failed! Image is not uploaded because image is not getting in the URL");
                throw new ImageUploadException("Image is not uploaded!");
            }

            originalFileName = profileImage.getOriginalFilename();

            metadata = new ObjectMetadata();
            metadata.setContentLength(profileImage.getSize());

            try {
                if(originalFileName != null){
                    fileName = UUID.randomUUID().toString()+originalFileName.substring(originalFileName.lastIndexOf("."));
                    amazonS3.putObject(bucketName,fileName,profileImage.getInputStream(),metadata);
                    url = amazonS3.getUrl(bucketName,fileName);
                    logger.info("Success! image store in cloud...");
                    return url.toString();
                }
            }catch (Exception e){
                logger.error("Failed! to uploaded image in cloud: {}",e.getMessage());
                throw new ImageUploadException("Failed! to upload image to AWS S3 " + e);
            }

        }catch(Exception e){
            logger.error("Failed! Image not uploaded: {} : {}",e.getMessage(),e.getStackTrace());
        }
        return null;
    }

    // Delete Image
    public boolean deleteImage(String fileUrl){
        String fileName;
        boolean status = false;
        try{
            if(fileUrl != null && !fileUrl.isEmpty()){
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName,fileName));
                return status=true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return status;
    }

}
