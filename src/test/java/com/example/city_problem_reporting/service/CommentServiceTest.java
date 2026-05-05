package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.CommentResponse;
import com.example.city_problem_reporting.dto.CreateCommentRequest;
import com.example.city_problem_reporting.model.Comment;
import com.example.city_problem_reporting.model.Post;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.CommentRepository;
import com.example.city_problem_reporting.repository.PostRepository;
import com.example.city_problem_reporting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CommentService commentService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("ana");
        post = new Post();
        post.setId(UUID.randomUUID());
        post.setUser(user);
    }

    @Test
    void addComment_validInput_savesAndReturns() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Се согласувам!");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CommentResponse response = commentService.addComment(post.getId(), req, "ana");
        assertThat(response.getContent()).isEqualTo("Се согласувам!");
        assertThat(response.getUsername()).isEqualTo("ana");
    }

    @Test
    void addComment_blankContent_throwsBadRequest() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("");
        assertThatThrownBy(() -> commentService.addComment(post.getId(), req, "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("content is required");
    }

    @Test
    void addComment_postNotFound_throws404() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("ok");
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commentService.addComment(post.getId(), req, "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void getComments_returnsListInOrder() {
        Comment c1 = sampleComment("first");
        Comment c2 = sampleComment("second");
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.findByPostOrderByCreatedAtAsc(post)).thenReturn(List.of(c1, c2));

        List<CommentResponse> responses = commentService.getComments(post.getId());
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CommentResponse::getContent)
                .containsExactly("first", "second");
    }

    private Comment sampleComment(String content) {
        Comment c = new Comment();
        c.setId(UUID.randomUUID());
        c.setUser(user);
        c.setPost(post);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}