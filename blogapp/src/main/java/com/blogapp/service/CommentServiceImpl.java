package com.blogapp.service;

import com.blogapp.entity.Comment;
import com.blogapp.entity.Post;
import com.blogapp.entity.User;
import com.blogapp.exception.CommentNotFountException;
import com.blogapp.exception.PostNotFoundException;
import com.blogapp.exception.UserNotFoundException;
import com.blogapp.payload.CommentDetailsDto;
import com.blogapp.payload.CommentDto;
import com.blogapp.repository.CommentRepository;
import com.blogapp.repository.PostRepository;
import com.blogapp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public CommentDetailsDto mapToDto(Comment comment) {
        return modelMapper.map(comment, CommentDetailsDto.class);
    }

    public Comment mapToEntity(CommentDto commentDto) {
        return modelMapper.map(commentDto, Comment.class);
    }

    @Override
    public CommentDetailsDto addComment(CommentDto commentDto) {
        logger.info("Attempting to add a comment for Post ID: {} by User ID: {}", commentDto.getPostId(), commentDto.getUserId());
        Comment comment = null;
        Post post;
        User user;
        Comment saved = null;
        try {
            comment = mapToEntity(commentDto);

            post = postRepository.findById(commentDto.getPostId()).orElseThrow(
                    () -> new PostNotFoundException("Post not exist! by id: " + commentDto.getPostId())
            );
            if(post != null){
                logger.info("Success! post was founded! by id: {}",post.getId());
                comment.setPost(post);
            }

            user = userRepository.findById(commentDto.getUserId()).orElseThrow(
                    () -> new UserNotFoundException("User not exist! by id: " + commentDto.getUserId())
            );
            if (user != null){
                logger.info("Success! user was founded! by id: {}",user.getId());
               comment.setUser(user);
            }

            comment.setCreateAt(LocalDateTime.now().withNano(0));
            comment.setUpdateAt(LocalDateTime.now().withNano(0));
            saved = commentRepository.save(comment);

            logger.info("Comment added successfully with ID: {}", saved.getId());
            return mapToDto(saved);
        } catch (Exception e) {
            logger.error("Failed to add comment: {}", e.getMessage());
        } finally {
            comment = null;
            post = null;
            user = null;
            saved = null;
        }
        return null;
    }

    @Override
    public String deleteComment(Long commentId) {
        logger.info("Attempting to delete comment with ID: {}", commentId);
        Comment comment = null;
        try {
            comment = commentRepository.findById(commentId).orElseThrow(
                    () -> new CommentNotFountException("Comment not found! by id: " + commentId)
            );
            if(comment != null){
                commentRepository.deleteById(commentId);
                logger.info("Comment with ID: {} deleted successfully.", commentId);
                return "Comment is deleted by comment id: " + commentId;
            }
            logger.warn("Comment with ID: {} not found.", commentId);
        } catch (Exception e) {
            logger.error("Failed to delete comment: {}", e.getMessage());
        } finally {
            comment = null;
        }
        return "Comment is not found!";
    }

    @Override
    public CommentDetailsDto updateComment(Long commentId, CommentDto commentDto) {
        logger.info("Attempting to update comment with ID: {}", commentId);
        Optional<Comment> opComment;
        Comment comment = null;
        Comment saved = null;
        try {
            comment = commentRepository.findById(commentId).orElseThrow(
                    () -> new CommentNotFountException("Comment not found! By id: " + commentId)
            );
            if(comment != null){
                comment.setComment(commentDto.getComment());
                comment.setPost(comment.getPost());
                comment.setUser(comment.getUser());
                comment.setCreateAt(comment.getCreateAt());
                comment.setUpdateAt(LocalDateTime.now().withNano(0));
                saved = commentRepository.save(comment);
                logger.info("Comment with ID: {} updated successfully.", commentId);
                return mapToDto(saved);
            }
            logger.warn("Comment with ID: {} not found.", commentId);
        } catch (Exception e) {
            logger.error("Failed to update comment: {}", e.getMessage());
        } finally {
            opComment = Optional.empty();
            comment = null;
            saved = null;
        }
        return null;
    }

    @Override
    public CommentDetailsDto findByCommentId(Long commentId) {
        logger.info("Fetching comment with ID: {}", commentId);
        Optional<Comment> opComment;
        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(
                    () -> new CommentNotFountException("Comment not found! By Id: " + commentId)
            );
            if (comment != null){
                logger.info("Comment with ID: {} found.", commentId);
                return mapToDto(comment);
            }
            logger.warn("Comment with ID: {} not found.", commentId);
        } catch (Exception e) {
            logger.error("Failed to fetch comment: {}", e.getMessage());
        } finally {
            opComment = Optional.empty();
        }
        return null;
    }

    @Override
    public List<CommentDetailsDto> listOfComments() {
        logger.info("Fetching list of all comments.");
        List<Comment> commentList = null;
        List<CommentDetailsDto> commentDtoList = null;
        try {
            commentList = commentRepository.findAll();
            if(!commentList.isEmpty()){
                logger.info("Fetched List Comments {} Data", commentList.size());
                commentDtoList = commentList.stream().map(this::mapToDto).collect(Collectors.toList());
                return commentDtoList;
            }
            logger.warn("Comment details not present!");
        } catch (Exception e) {
            logger.error("Failed to fetch comments: {}", e.getMessage());
        } finally {
            commentList = null;
            commentDtoList = null;
        }
        return null;
    }
}
