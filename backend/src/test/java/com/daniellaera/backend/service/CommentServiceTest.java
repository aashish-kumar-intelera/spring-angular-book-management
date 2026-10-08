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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    private Comment comment;

    @BeforeEach
    public void setUp() {
        commentService = new CommentServiceImpl(commentRepository, userRepository, bookRepository);
        User owner = User.builder().email("owner@example.com").firstName("Owner").lastName("User").build();
        comment = Comment.builder().id(1).user(owner).content("original").build();
    }

    @Test
    public void updateComment_byOwner_updatesContent() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));
        CommentDTO dto = new CommentDTO();
        dto.setContent("edited");

        CommentDTO result = commentService.updateComment(1, "owner@example.com", dto);

        assertThat(result.getContent()).isEqualTo("edited");
    }

    @Test
    public void updateComment_byOtherUser_isForbidden() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));
        CommentDTO dto = new CommentDTO();
        dto.setContent("hijacked");

        assertThatThrownBy(() -> commentService.updateComment(1, "attacker@example.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(comment.getContent()).isEqualTo("original");
        verify(commentRepository, never()).save(any());
    }

    @Test
    public void deleteComment_byOwner_deletes() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1, "owner@example.com");

        verify(commentRepository).delete(comment);
    }

    @Test
    public void deleteComment_byOtherUser_isForbidden() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(1, "attacker@example.com"))
                .isInstanceOf(ResponseStatusException.class);
        verify(commentRepository, never()).delete(any());
    }
}
