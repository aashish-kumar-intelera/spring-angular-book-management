package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.CommentRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setFirstName("Owner");
        owner.setLastName("User");
        comment = Comment.builder().id(7).content("original").user(owner).build();
        given(commentRepository.findById(7)).willReturn(Optional.of(comment));
    }

    @Test
    void updateComment_ByOwner_UpdatesAndReturnsId() {
        given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

        CommentDTO result = commentService.updateComment(7, "owner@example.com", CommentDTO.builder().content("edited").build());

        assertEquals(7, result.getId());
        assertEquals("edited", result.getContent());
    }

    @Test
    void updateComment_ByOtherUser_IsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.updateComment(7, "other@example.com", CommentDTO.builder().content("hacked").build()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("original", comment.getContent());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_ByOtherUser_IsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.deleteComment(7, "other@example.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_ByOwner_Deletes() {
        commentService.deleteComment(7, "owner@example.com");

        verify(commentRepository).delete(comment);
    }
}
