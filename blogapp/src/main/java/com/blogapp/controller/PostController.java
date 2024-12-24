package com.blogapp.controller;

import com.blogapp.exception.ImagesLimitExceedException;
import com.blogapp.payload.PostDetailsDto;
import com.blogapp.payload.PostDto;
import com.blogapp.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/post")
public class PostController {
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
        if(postImages.size() <= 3){
            PostDetailsDto post = postService.addPost(postDto,postImages);
            return new ResponseEntity<>(post, HttpStatus.CREATED);
        }else{
            throw new ImagesLimitExceedException("You can upload maximum 3 images!");
        }
    }

    //http://localhost:8080/api/v1/post/deletePost/{postId}
    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long postId
    ){
        String deletePost = postService.deletePostDetails(postId);
        return new ResponseEntity<>(deletePost, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/post/updatePost/{postId}
    @PutMapping("/updatePost/{postId}")
    public ResponseEntity<PostDetailsDto> updatePost(
            @PathVariable Long postId,
            @RequestPart("postDto") PostDto postDto,
            @RequestPart("file") List<MultipartFile> postImages
    ){
        if(postImages.size() <= 3){
            PostDetailsDto updatePost = postService.updatePost(postId,postDto,postImages);
            return new ResponseEntity<>(updatePost, HttpStatus.OK);
        }else{
            throw new ImagesLimitExceedException("You can upload maximum 3 images!");
        }
    }

    //http://localhost:8080/api/v1/post/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsDto> findByPostId(
            @PathVariable Long postId
    ){
        PostDetailsDto post = postService.findByPostId(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/post
    @GetMapping
    public ResponseEntity<List<PostDetailsDto>> getPosts(){
        List<PostDetailsDto> posts = postService.listOfPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

}
