package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.ClassificationResult;
import com.example.city_problem_reporting.dto.CreatePostRequest;
import com.example.city_problem_reporting.dto.PostResponse;
import com.example.city_problem_reporting.model.Post;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.CommentRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClassificationService classificationService;
    @Mock private LikeRepository likeRepository;
    @Mock private CommentRepository commentRepository;
    @InjectMocks private PostService postService;

    private User author;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(UUID.randomUUID());
        author.setUsername("ana");
        author.setEmail("ana@test.com");
    }

    @Test
    void createPost_withImage_classifiesFromImage() {
        CreatePostRequest req = new CreatePostRequest();
        req.setDescription("Дупка пред зграда");
        req.setImageUrl("http://cdn/photo.jpg");
        req.setLatitude(new BigDecimal("41.99"));
        req.setLongitude(new BigDecimal("21.43"));
        req.setCategory("ignored_when_image_present");

        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(author));
        ClassificationResult result = new ClassificationResult();
        result.setCategory("road_damage");
        when(classificationService.classifyFromUrl("http://cdn/photo.jpg")).thenReturn(result);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            if (p.getId() == null) p.setId(UUID.randomUUID());
            return p;
        });
        // findAll() се повикува внатре во incrementNearbyPriority — врати празна листа
        when(postRepository.findAll()).thenReturn(List.of());

        PostResponse response = postService.createPost(req, "ana");

        assertThat(response.getCategory()).isEqualTo("road_damage");
        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getPriorityScore()).isEqualTo(0);
        verify(classificationService).classifyFromUrl("http://cdn/photo.jpg");
    }

    @Test
    void createPost_withoutImage_usesProvidedCategory() {
        CreatePostRequest req = new CreatePostRequest();
        req.setDescription("Скршен семафор");
        req.setCategory("traffic_signal");
        req.setLatitude(new BigDecimal("41.99"));
        req.setLongitude(new BigDecimal("21.43"));

        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            if (p.getId() == null) p.setId(UUID.randomUUID());
            return p;
        });
        when(postRepository.findAll()).thenReturn(List.of());

        PostResponse response = postService.createPost(req, "ana");

        assertThat(response.getCategory()).isEqualTo("traffic_signal");
        verifyNoInteractions(classificationService);
    }

    @Test
    void createPost_blankDescription_throwsBadRequest() {
        CreatePostRequest req = new CreatePostRequest();
        req.setDescription("   ");
        assertThatThrownBy(() -> postService.createPost(req, "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("description is required");
    }

    @Test
    void createPost_unknownUser_throwsUnauthorized() {
        CreatePostRequest req = new CreatePostRequest();
        req.setDescription("опис");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.createPost(req, "ghost"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authenticated user");
    }

    @Test
    void getAllPosts_returnsMappedResponses() {
        Post p1 = samplePost("road_damage", "OPEN");
        Post p2 = samplePost("waste", "RESOLVED");
        when(postRepository.findAll()).thenReturn(List.of(p1, p2));
        when(likeRepository.countByPost(any(Post.class))).thenReturn(3);
        when(commentRepository.countByPost(any(Post.class))).thenReturn(2);

        List<PostResponse> responses = postService.getAllPosts();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(PostResponse::getCategory)
                .containsExactly("road_damage", "waste");
        assertThat(responses).allMatch(r -> r.getLikeCount() == 3);
        assertThat(responses).allMatch(r -> r.getCommentCount() == 2);
    }

    @Test
    void getPostsByUserId_filters() {
        UUID userId = author.getId();
        Post p = samplePost("flooding", "OPEN");
        when(postRepository.findByUser_Id(userId)).thenReturn(List.of(p));
        when(likeRepository.countByPost(any(Post.class))).thenReturn(0);
        when(commentRepository.countByPost(any(Post.class))).thenReturn(0);

        List<PostResponse> responses = postService.getPostsByUserId(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategory()).isEqualTo("flooding");
    }

    @Test
    void updateReportStatus_validStatus_persists() {
        UUID postId = UUID.randomUUID();
        Post post = samplePost("road_damage", "OPEN");
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        PostResponse response = postService.updateReportStatus(postId, "RESOLVED", "ana");
        assertThat(response.getStatus()).isEqualTo("RESOLVED");
    }

    @Test
    void updateReportStatus_postMissing_throws404() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.updateReportStatus(postId, "RESOLVED", "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void updateReportStatus_blankStatus_throwsBadRequest() {
        UUID postId = UUID.randomUUID();
        Post post = samplePost("waste", "OPEN");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(author));
        assertThatThrownBy(() -> postService.updateReportStatus(postId, "  ", "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Status cannot be empty");
    }

    @Test
    void updateReportPriority_negative_throwsBadRequest() {
        UUID postId = UUID.randomUUID();
        Post post = samplePost("waste", "OPEN");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(author));
        assertThatThrownBy(() -> postService.updateReportPriority(postId, -1, "ana"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Priority must be non-negative");
    }

    @Test
    void updateReportPriority_validValue_persists() {
        UUID postId = UUID.randomUUID();
        Post post = samplePost("waste", "OPEN");
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        PostResponse response = postService.updateReportPriority(postId, 5, "ana");
        assertThat(response.getPriorityScore()).isEqualTo(5);
    }

    private Post samplePost(String category, String status) {
        Post p = new Post();
        p.setId(UUID.randomUUID());
        p.setUser(author);
        p.setDescription("desc");
        p.setCategory(category);
        p.setStatus(status);
        p.setPriorityScore(0);
        p.setLatitude(new BigDecimal("41.99"));
        p.setLongitude(new BigDecimal("21.43"));
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }
}