package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.CommentDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    List<CommentDTO> getCommentsByBookId(Integer postId);

    CommentDTO createCommentByBookIdAndUserId(Integer bookId, String userEmail, CommentDTO commentDTO);

    CommentDTO updateComment(Integer commentId, String userEmail, CommentDTO commentDTO);

    void deleteComment(Integer commentId, String userEmail);
}
