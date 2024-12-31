package com.xplore.controller;

import com.xplore.exception.ImageUploadException;
import com.xplore.exception.ImagesLimitExceedException;
import com.xplore.exception.PostNotFoundException;
import com.xplore.payload.PostDetailsDto;
import com.xplore.payload.PostDto;
import com.xplore.service.PostService;
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

    private final PostService postService;
    public PostController(PostService postService){
        this.postService = postService;
    }

    //http://localhost:8080/api/v1/post/addPost
    @PostMapping("/addPost")
    public ResponseEntity<PostDetailsDto> createPost(
            @Valid @RequestPart("postDto") PostDto postDto,
            @RequestPart("file") List<MultipartFile> postImages
    ){
        logger.info("Entering post object: {}", postDto.getId());
        PostDetailsDto post = null;
        if(!postImages.isEmpty() && !postImages.get(0).getOriginalFilename().isBlank()) {
            logger.info("Success! images is coming in the URL! {}",getPosts());
            if (!postImages.isEmpty() && postImages.size() <= 3) {
                logger.info("Successfully retrieved post images: {}",postImages);
                post = postService.addPost(postDto, postImages);
                if (post != null){
                    return new ResponseEntity<>(post, HttpStatus.CREATED);
                }
                throw new PostNotFoundException("Failed! Post not created!");
            } else {
                ImagesLimitExceedException e = new ImagesLimitExceedException("You can upload maximum 3 images!");
                logger.error("Failed! Images Limit Exceed: {} : {}", e.getMessage(), e.getStackTrace());
                throw new ImagesLimitExceedException("You can upload maximum 3 images!");
            }
        }else {
            logger.error("Failed! to retrieve post images: {}",postImages);
            throw new ImageUploadException("You can upload images!");
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
            throw new PostNotFoundException("Failed! Post not found! By Id: "+postId);
        }
    }

    //http://localhost:8080/api/v1/post/updatePost/{postId}
    @PutMapping("/updatePost/{postId}")
    public ResponseEntity<PostDetailsDto> updatePost(
            @PathVariable Long postId,
            @RequestPart("postDto") PostDto postDto,
            @RequestPart("file") List<MultipartFile> postImages
    ){
        logger.info("Update post object: {}", postDto.getId());
        PostDetailsDto updatePost = null;

        if(!postImages.isEmpty() && !postImages.get(0).getOriginalFilename().isBlank()) {
            logger.info("Success! images is coming in this URL!");
            if (!postImages.isEmpty() && postImages.size() <= 3) {
                logger.info("Successfully retrieved update post images: {}", postImages.size());
                updatePost = postService.updatePost(postId, postDto, postImages);
                if(updatePost != null){
                    return new ResponseEntity<>(updatePost, HttpStatus.OK);
                }
                logger.warn("Post was not updated! : {}",postId);
                throw new PostNotFoundException("Failed! Post not found! By Id: "+postId);
            } else {
                ImagesLimitExceedException e = new ImagesLimitExceedException("You can upload maximum 3 images!");
                logger.error("Failed! New Images Limit Exceed: {} : {}", e.getMessage(), e.getStackTrace());
                throw new ImagesLimitExceedException("You can upload maximum 3 images!");
            }
        }else{
            logger.error("Failed! to retrieve update post images: {}",postImages);
            throw new ImageUploadException("You can upload images!");
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
            if(post != null){
                return new ResponseEntity<>(post, HttpStatus.OK);
            }
            logger.warn("Post not found! By Id: {}",postId);
            throw new PostNotFoundException("Failed! Post not found! By Id: "+postId);
        }else{
            logger.error("Failed! Get the post id is not correct: {}", postId);
            throw new PostNotFoundException("Failed! Post not found! By Id: " + postId);
        }
    }

    //http://localhost:8080/api/v1/post
    @GetMapping
    public ResponseEntity<List<PostDetailsDto>> getPosts(){
        List<PostDetailsDto> posts = null;
        try{
            posts = postService.listOfPosts();
            if(posts!=null){
                logger.info("Getting all the posts object: {}",posts.size());
                return new ResponseEntity<>(posts, HttpStatus.OK);
            }
            logger.warn("Posts not found!");
        }catch (Exception e){
            logger.error("Post's was not founded! : {} : {}",e.getMessage(),e.getStackTrace());
        }
        throw new PostNotFoundException("Posts not found!");
    }
}
