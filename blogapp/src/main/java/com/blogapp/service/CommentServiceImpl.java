package com.blogapp.service;

import com.blogapp.entity.Comment;
import com.blogapp.entity.Post;
import com.blogapp.entity.User;
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
        Optional<Post> opPost;
        Optional<User> opUser;
        Comment saved = null;
        try {
            comment = mapToEntity(commentDto);
            opPost = postRepository.findById(commentDto.getPostId());
            if (opPost.isPresent()) {
                comment.setPost(opPost.get());
            }

            opUser = userRepository.findById(commentDto.getUserId());
            if (opUser.isPresent()) {
                comment.setUser(opUser.get());
            }

            comment.setCreateAt(LocalDateTime.now().withNano(0));
            comment.setUpdateAt(LocalDateTime.now().withNano(0));
            saved = commentRepository.save(comment);

            logger.info("Comment added successfully with ID: {}", saved.getId());
            return mapToDto(saved);
        } catch (Exception e) {
            logger.error("Failed to add comment: {}", e.getMessage(), e);
            throw e;
        } finally {
            comment = null;
            opPost = Optional.empty();
            opUser = Optional.empty();
            saved = null;
        }
    }

    @Override
    public String deleteComment(Long commentId) {
        logger.info("Attempting to delete comment with ID: {}", commentId);
        Optional<Comment> opComment;
        try {
            opComment = commentRepository.findById(commentId);
            if (opComment.isPresent()) {
                commentRepository.deleteById(commentId);
                logger.info("Comment with ID: {} deleted successfully.", commentId);
                return "Comment is deleted by comment id: " + commentId;
            }
            logger.warn("Comment with ID: {} not found.", commentId);
            return "Comment is not found!";
        } catch (Exception e) {
            logger.error("Failed to delete comment: {}", e.getMessage(), e);
            throw e;
        } finally {
            opComment = Optional.empty();
        }
    }

    @Override
    public CommentDetailsDto updateComment(Long commentId, CommentDto commentDto) {
        logger.info("Attempting to update comment with ID: {}", commentId);
        Optional<Comment> opComment;
        Comment comment = null;
        Comment saved = null;
        try {
            opComment = commentRepository.findById(commentId);
            if (opComment.isPresent()) {
                comment = opComment.get();
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
            return null;
        } catch (Exception e) {
            logger.error("Failed to update comment: {}", e.getMessage(), e);
            throw e;
        } finally {
            opComment = Optional.empty();
            comment = null;
            saved = null;
        }
    }

    @Override
    public CommentDetailsDto findByCommentId(Long commentId) {
        logger.info("Fetching comment with ID: {}", commentId);
        Optional<Comment> opComment;
        try {
            opComment = commentRepository.findById(commentId);
            if (opComment.isPresent()) {
                logger.info("Comment with ID: {} found.", commentId);
                return mapToDto(opComment.get());
            }
            logger.warn("Comment with ID: {} not found.", commentId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch comment: {}", e.getMessage(), e);
            throw e;
        } finally {
            opComment = Optional.empty();
        }
    }

    @Override
    public List<CommentDetailsDto> listOfComments() {
        logger.info("Fetching list of all comments.");
        List<Comment> commentList = null;
        List<CommentDetailsDto> commentDtoList = null;
        try {
            commentList = commentRepository.findAll();
            logger.info("Fetched {} comments from the database.", commentList.size());
            commentDtoList = commentList.stream().map(this::mapToDto).collect(Collectors.toList());
            return commentDtoList;
        } catch (Exception e) {
            logger.error("Failed to fetch comments: {}", e.getMessage(), e);
            throw e;
        } finally {
            commentList = null;
            commentDtoList = null;
        }
    }
}
