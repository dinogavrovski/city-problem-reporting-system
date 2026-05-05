package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.LikeResponse;
import com.example.city_problem_reporting.model.Like;
import com.example.city_problem_reporting.model.Post;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.LikeRepository;
import com.example.city_problem_reporting.repository.PostRepository;
import com.example.city_problem_reporting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock private LikeRepository likeRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private LikeService likeService;

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
    void likePost_firstTime_savesLike() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(likeRepository.existsByPostAndUser(post, user)).thenReturn(false);
        when(likeRepository.countByPost(post)).thenReturn(1);

        LikeResponse response = likeService.likePost(post.getId(), "ana");
        assertThat(response.getLikeCount()).isEqualTo(1);
        assertThat(response.isLikedByMe()).isTrue();
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void likePost_alreadyLiked_doesNotSaveAgain() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(likeRepository.existsByPostAndUser(post, user)).thenReturn(true);
        when(likeRepository.countByPost(post)).thenReturn(1);

        LikeResponse response = likeService.likePost(post.getId(), "ana");
        assertThat(response.getLikeCount()).isEqualTo(1);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void unlikePost_existing_removes() {
        Like like = new Like();
        like.setId(UUID.randomUUID());
        like.setUser(user);
        like.setPost(post);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(likeRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(like));
        when(likeRepository.countByPost(post)).thenReturn(0);

        LikeResponse response = likeService.unlikePost(post.getId(), "ana");
        assertThat(response.getLikeCount()).isEqualTo(0);
        assertThat(response.isLikedByMe()).isFalse();
        verify(likeRepository).delete(like);
    }

    @Test
    void unlikePost_whenNoLike_isNoOp() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(likeRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());
        when(likeRepository.countByPost(post)).thenReturn(0);

        LikeResponse response = likeService.unlikePost(post.getId(), "ana");
        assertThat(response.getLikeCount()).isEqualTo(0);
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void getLikes_returnsCountAndStatus() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        when(likeRepository.countByPost(post)).thenReturn(5);
        when(likeRepository.existsByPostAndUser(post, user)).thenReturn(true);

        LikeResponse response = likeService.getLikes(post.getId(), "ana");
        assertThat(response.getLikeCount()).isEqualTo(5);
        assertThat(response.isLikedByMe()).isTrue();
    }

    @Test
    void likePost_postMissing_throws404() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> likeService.likePost(post.getId(), "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }
}