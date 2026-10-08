package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v3/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{bookId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByBookId(@PathVariable Integer bookId) {
        List<CommentDTO> comments = commentService.getCommentsByBookId(bookId);
        return ResponseEntity.ok().body(comments);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{bookId}")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Integer bookId,
            @RequestBody CommentDTO commentDTO,
            @AuthenticationPrincipal User currentUser // logged-in user
    )  {
        if (currentUser.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = currentUser.getEmail();
        return ResponseEntity.ok(commentService.createCommentByBookIdAndUserId(bookId, userEmail, commentDTO));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/edit/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Integer commentId,
            @RequestBody CommentDTO commentDTO,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, currentUser.getEmail(), commentDTO));
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal User currentUser
    ) {
        commentService.deleteComment(commentId, currentUser.getEmail());
        return ResponseEntity.noContent().build();
    }
}
