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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public CommentDetailsDto mapToDto(Comment comment){
        return modelMapper.map(comment,CommentDetailsDto.class);
    }

    public Comment mapToEntity(CommentDto commentDto){
        return modelMapper.map(commentDto,Comment.class);
    }

    @Override
    public CommentDetailsDto addComment(CommentDto commentDto) {
        Comment comment = mapToEntity(commentDto);

        Optional<Post> opPost = postRepository.findById(commentDto.getPostId());
        if(opPost.isPresent()){
            comment.setPost(opPost.get());
        }

        Optional<User> opUser = userRepository.findById(commentDto.getUserId());
        if(opUser.isPresent()){
            comment.setUser(opUser.get());
        }

        comment.setCreateAt(LocalDateTime.now().withNano(0));
        comment.setUpdateAt(LocalDateTime.now().withNano(0));
        Comment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    @Override
    public String deleteComment(Long commentId) {
        Optional<Comment> opComment = commentRepository.findById(commentId);
        if(opComment.isPresent()){
            commentRepository.deleteById(commentId);
            return "Comment is deleted by comment id : "+commentId;
        }
        return "comment is not found!";
    }

    @Override
    public CommentDetailsDto updateComment(Long commentId, CommentDto commentDto) {
        Optional<Comment> opComment = commentRepository.findById(commentId);
        if(opComment.isPresent()){
            Comment comment = opComment.get();

//            Optional<Post> opPost = postRepository.findById(commentDto.getPostId());
//            if(opPost.isPresent()){
//                comment.setPost(opPost.get());
//            }

//            Optional<User> opUser = userRepository.findById(commentDto.getUserId());
//            if(opUser.isPresent()){
//                comment.setUser(opUser.get());
//            }
            comment.setComment(commentDto.getComment());
            comment.setPost(comment.getPost());
            comment.setUser(comment.getUser());

            comment.setCreateAt(comment.getCreateAt());

            comment.setUpdateAt(LocalDateTime.now().withNano(0));
            Comment saved = commentRepository.save(comment);
            return mapToDto(saved);
        }
        return null;
    }

    @Override
    public CommentDetailsDto findByCommentId(Long commentId) {
        Optional<Comment> opComment = commentRepository.findById(commentId);
        if(opComment.isPresent()){
            return mapToDto(opComment.get());
        }
        return null;
    }

    @Override
    public List<CommentDetailsDto> listOfComments() {
        List<Comment> commentList = commentRepository.findAll();
        return commentList.stream().map(comment->mapToDto(comment)).collect(Collectors.toList());
    }
}
