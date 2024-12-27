package com.blogapp.controller;

import com.blogapp.exception.ImagesLimitExceedException;
import com.blogapp.payload.PostDetailsDto;
import com.blogapp.payload.PostDto;
import com.blogapp.service.PostService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/post")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private PostService postService;
    public PostController(PostService postService){
        this.postService = postService;
    }

    //http://localhost:8080/api/v1/post/addPost
    @PostMapping("/addPost")
    public ResponseEntity<PostDetailsDto> createPost(
            @Valid @RequestPart("postDto") PostDto postDto,
            @RequestPart("file") List<MultipartFile> postImages
    ){
        logger.info("Entering post object: {}", postDto);
        PostDetailsDto post = null;
        if(postImages != null) {
            logger.info("Success! images is coming on this URL!");
            if (postImages.size() <= 3) {
                logger.info("Successfully retrieved post images: {}", postImages);
                post = postService.addPost(postDto, postImages);
                return new ResponseEntity<>(post, HttpStatus.CREATED);
            } else {
                ImagesLimitExceedException e = new ImagesLimitExceedException("You can upload maximum 3 images!");
                logger.error("Failed! Images Limit Exceed: {} : {}", e.getMessage(), e.getStackTrace());
                throw new ImagesLimitExceedException("You can upload maximum 3 images!");
            }
        }else {
            logger.error("Failed! to retrieve post images: {}",postImages);
            return new ResponseEntity<>(post, HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/v1/post/deletePost/{postId}
    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long postId
    ){
        if(postId != null){
            logger.info("Successfully getting post id: {}",postId);
            String deletePost = postService.deletePostDetails(postId);
            return new ResponseEntity<>(deletePost, HttpStatus.OK);
        }else{
            logger.error("Failed! post id is not getting: {}", postId);
            return new ResponseEntity<>("Post id not getting...", HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/v1/post/updatePost/{postId}
    @PutMapping("/updatePost/{postId}")
    public ResponseEntity<PostDetailsDto> updatePost(
            @PathVariable Long postId,
            @RequestPart("postDto") PostDto postDto,
            @RequestPart("file") List<MultipartFile> postImages
    ){
        logger.info("Update post object: {}", postDto);
        PostDetailsDto updatePost = null;

        if(postImages != null) {
            logger.info("Success! images is coming on this URL!");
            if (postImages.size() <= 3) {
                logger.info("Successfully retrieved update post images: {}", postImages);
                updatePost = postService.updatePost(postId, postDto, postImages);
                return new ResponseEntity<>(updatePost, HttpStatus.OK);
            } else {
                ImagesLimitExceedException e = new ImagesLimitExceedException("You can upload maximum 3 images!");
                logger.error("Failed! New Images Limit Exceed: {} : {}", e.getMessage(), e.getStackTrace());
                throw new ImagesLimitExceedException("You can upload maximum 3 images!");
            }
        }else{
            logger.error("Failed! to retrieve update post images: {}",postImages);
            return new ResponseEntity<>(updatePost, HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/v1/post/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsDto> findByPostId(
            @PathVariable Long postId
    ){
        PostDetailsDto post = null;
        if(postId != null){
            logger.info("Success! Get the post id is: {}",postId);
            post = postService.findByPostId(postId);
            return new ResponseEntity<>(post, HttpStatus.OK);
        }else{
            logger.error("Failed! Get the post id is not correct: {}", postId);
            return new ResponseEntity<>(post,HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/v1/post
    @GetMapping
    public ResponseEntity<List<PostDetailsDto>> getPosts(){
        List<PostDetailsDto> posts = postService.listOfPosts();
        logger.info("Getting all the posts object: {}",posts);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

}
