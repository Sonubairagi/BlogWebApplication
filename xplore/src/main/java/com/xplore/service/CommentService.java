package com.xplore.service;

import com.xplore.payload.CommentDetailsDto;
import com.xplore.payload.CommentDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    CommentDetailsDto addComment(CommentDto commentDto);

    String deleteComment(Long commentId);

    CommentDetailsDto updateComment(Long commentId, CommentDto commentDto);

    CommentDetailsDto findByCommentId(Long commentId);

    List<CommentDetailsDto> listOfComments();
}
