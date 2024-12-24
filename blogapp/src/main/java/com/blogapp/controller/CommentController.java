package com.blogapp.controller;

import com.blogapp.payload.CommentDetailsDto;
import com.blogapp.payload.CommentDto;
import com.blogapp.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/comment")
public class CommentController {

    private CommentService commentService;
    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    //http://localhost:8080/api/v1/comment/addComment
    @PostMapping("/addComment")
    public ResponseEntity<CommentDetailsDto> addComment(
            @Valid @RequestBody CommentDto commentDto
    ){
        CommentDetailsDto comment = commentService.addComment(commentDto);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    //http://localhost:8080/api/v1/comment/deleteComment/{commentId}
    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId
    ){
        String deleteComment = commentService.deleteComment(commentId);
        return new ResponseEntity<>(deleteComment, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/comment/updateComment/{commentId}
    @PutMapping("/updateComment/{commentId}")
    public ResponseEntity<CommentDetailsDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto
    ){
        CommentDetailsDto updateComment = commentService.updateComment(commentId,commentDto);
        return new ResponseEntity<>(updateComment, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/comment/{commentId}
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDetailsDto> findByCommentId(
            @PathVariable Long commentId
    ){
        CommentDetailsDto comment = commentService.findByCommentId(commentId);
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/comment
    @GetMapping
    public ResponseEntity<List<CommentDetailsDto>> getComments(){
        List<CommentDetailsDto> comments = commentService.listOfComments();
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

}
