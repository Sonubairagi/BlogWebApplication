package com.xplore.controller;

import com.xplore.payload.CommentDetailsDto;
import com.xplore.payload.CommentDto;
import com.xplore.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/comment")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private CommentService commentService;
    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    //http://localhost:8080/api/v1/comment/addComment
    @PostMapping("/addComment")
    public ResponseEntity<CommentDetailsDto> addComment(
            @Valid @RequestBody CommentDto commentDto
    ){
        logger.info("Success! Entering Comment object: {}", commentDto.getComment());
        CommentDetailsDto comment = null;
        try {
            comment = commentService.addComment(commentDto);
            if (comment != null){
                return new ResponseEntity<>(comment, HttpStatus.CREATED);
            }
        }catch (Exception e){
            logger.error("Comment details has not right: {}",e.getMessage());
        }
        return new ResponseEntity<>(comment, HttpStatus.BAD_REQUEST);
    }

    //http://localhost:8080/api/v1/comment/deleteComment/{commentId}
    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId
    ){
        if(commentId != null){
            logger.info("Successfully getting comment id: {}",commentId);
            String deleteComment = commentService.deleteComment(commentId);
            return new ResponseEntity<>(deleteComment, HttpStatus.OK);
        }else {
            logger.error("Failed! Comment id is not getting: {}", commentId);
            return new ResponseEntity<>("Comment id not getting...", HttpStatus.BAD_REQUEST);
        }

    }

    //http://localhost:8080/api/v1/comment/updateComment/{commentId}
    @PutMapping("/updateComment/{commentId}")
    public ResponseEntity<CommentDetailsDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto
    ){
        logger.info("Update Comment details: {}", commentDto.getId());
        CommentDetailsDto updateComment = null;
        if(commentId != null){
            logger.info("Success! Updated Category id is: {}",commentId);
            updateComment = commentService.updateComment(commentId,commentDto);
            if(updateComment != null){
                return new ResponseEntity<>(updateComment, HttpStatus.OK);
            }
            return new ResponseEntity<>(updateComment, HttpStatus.BAD_REQUEST);
        }else {
            logger.error("Failed! Comment id is not correct: {}", commentId);
            return new ResponseEntity<>(updateComment, HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/v1/comment/{commentId}
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDetailsDto> findByCommentId(
            @PathVariable Long commentId
    ){
        CommentDetailsDto comment = null;
        if(commentId != null) {
            logger.info("Success! Get the comment id is: {}", commentId);
            comment = commentService.findByCommentId(commentId);
            if(comment != null){
                return new ResponseEntity<>(comment, HttpStatus.OK);
            }
            logger.warn("Comment not found! By Id: {}",commentId);
            return new ResponseEntity<>(comment, HttpStatus.BAD_REQUEST);
        }else{
            logger.error("Failed! Get the comment id is not correct: {}", commentId);
            return new ResponseEntity<>(comment, HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/v1/comment
    @GetMapping
    public ResponseEntity<List<CommentDetailsDto>> getComments(){
        List<CommentDetailsDto> comments = null;
        try {
            comments = commentService.listOfComments();
            if (!comments.isEmpty()){
                logger.info("Getting all the comments objects: {}",comments);
                return new ResponseEntity<>(comments, HttpStatus.OK);
            }
        }catch (Exception e){
            logger.error("Comments not found!");
        }
        return new ResponseEntity<>(comments, HttpStatus.BAD_REQUEST);
    }

}
