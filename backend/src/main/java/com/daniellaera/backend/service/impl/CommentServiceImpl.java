package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.CommentRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<CommentDTO> getCommentsByBookId(Integer bookId) {
        List<Comment> comments = commentRepository.findAllByBookId(bookId);

        return comments
                .stream()
                .map(this::convertCommentEntityToCommentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDTO createCommentByBookIdAndUserId(Integer bookId, String userEmail, CommentDTO commentDTO) {
        log.info("Loading user by username: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", userEmail);
                    return new UsernameNotFoundException("User not found with email: " + userEmail);
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", bookId);
                    return new EntityNotFoundException("Book not found with ID: " + bookId);
                });

        Comment comment = Comment.builder()
                .book(book)
                .user(user)
                .content(commentDTO.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return convertCommentEntityToCommentDTO(savedComment);
    }

    @Override
    public CommentDTO updateComment(Integer commentId, String userEmail, CommentDTO commentDTO) {
        log.info("User {} is editing comment {}", userEmail, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));
        checkOwnership(comment, userEmail);

        comment.setContent(commentDTO.getContent());
        return convertCommentEntityToCommentDTO(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Integer commentId, String userEmail) {
        log.info("User {} is deleting comment {}", userEmail, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));
        checkOwnership(comment, userEmail);

        commentRepository.delete(comment);
    }

    private void checkOwnership(Comment comment, String userEmail) {
        if (userEmail == null || !userEmail.equals(comment.getUser().getEmail())) {
            log.warn("User {} is not allowed to modify comment {}", userEmail, comment.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own comments");
        }
    }

    private CommentDTO convertCommentEntityToCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setAuthorFullName(comment.getUser().getFullName());
        return commentDTO;
    }
}
