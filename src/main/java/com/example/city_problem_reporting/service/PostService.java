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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PostService {
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final ClassificationService classificationService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       ClassificationService classificationService,
                       LikeRepository likeRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.classificationService = classificationService;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "city-problem-reporting-app")
                .build();
    }

//    public PostResponse createPost(CreatePostRequest request, String authenticatedUsername) {
//        if (request.getDescription() == null || request.getDescription().isBlank()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "description is required");
//        }
//
//        User user = userRepository.findByUsername(authenticatedUsername)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
//                        "Authenticated user was not found in database"));
//
//        Coordinates coordinates = resolveCoordinates(request);
//
//        Post post = new Post();
//        post.setUser(user);
//        post.setDescription(request.getDescription());
//        post.setImageUrl(request.getImageUrl());
//        post.setLatitude(coordinates.latitude());
//        post.setLongitude(coordinates.longitude());
//        post.setPriorityScore(0);
//        post.setStatus("OPEN");
//        post.setCreatedAt(LocalDateTime.now());
//
//        // Auto-classify from image, fallback to manual category
//        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
//            ClassificationResult result = classificationService.classifyFromUrl(request.getImageUrl());
//            post.setCategory(result.getCategory());
//        } else {
//            post.setCategory(request.getCategory() != null ? request.getCategory() : "other");
//        }
//
//        Post savedPost = postRepository.save(post);
//        return PostResponse.fromPost(savedPost);
//        // likeCount and commentCount are 0 for a brand new post — no need to query
//    }

public PostResponse createPost(CreatePostRequest request, String authenticatedUsername) {
    if (request.getDescription() == null || request.getDescription().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "description is required");
    }

    User user = userRepository.findByUsername(authenticatedUsername)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found in database"));

    Coordinates coordinates = resolveCoordinates(request);

    Post post = new Post();
    post.setUser(user);
    post.setDescription(request.getDescription());
    post.setImageUrl(request.getImageUrl());
    post.setLatitude(coordinates.latitude());
    post.setLongitude(coordinates.longitude());
    post.setPriorityScore(0);
    post.setStatus("OPEN");
    post.setCreatedAt(LocalDateTime.now());

    // Auto-classify from image, fallback to manual category
    if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
        ClassificationResult result = classificationService.classifyFromUrl(request.getImageUrl());
        post.setCategory(result.getCategory());
    } else {
        post.setCategory(request.getCategory() != null ? request.getCategory() : "other");
    }

    Post savedPost = postRepository.save(post);

    // Increment priority of nearby posts with the same category
    if (coordinates.latitude() != null && coordinates.longitude() != null
            && savedPost.getCategory() != null) {
        incrementNearbyPriority(savedPost);
    }

    return PostResponse.fromPost(savedPost);
}

    private void incrementNearbyPriority(Post newPost) {
        // Radius threshold — about 100 meters in degrees
        double threshold = 0.001;

        double newLat = newPost.getLatitude().doubleValue();
        double newLng = newPost.getLongitude().doubleValue();

        List<Post> nearbyPosts = postRepository.findAll().stream()
                .filter(p -> !p.getId().equals(newPost.getId()))
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .filter(p -> p.getCategory() != null
                        && p.getCategory().equals(newPost.getCategory()))
                .filter(p -> {
                    double latDiff = Math.abs(p.getLatitude().doubleValue() - newLat);
                    double lngDiff = Math.abs(p.getLongitude().doubleValue() - newLng);
                    return latDiff < threshold && lngDiff < threshold;
                })
                .toList();

        for (Post nearby : nearbyPosts) {
            nearby.setPriorityScore(
                    (nearby.getPriorityScore() == null ? 0 : nearby.getPriorityScore()) + 1
            );
            postRepository.save(nearby);
            System.out.println("Priority bumped for post: " + nearby.getId()
                    + " → " + nearby.getPriorityScore());
        }
    }

    private Coordinates resolveCoordinates(CreatePostRequest request) {
        if (request.getLatitude() != null && request.getLongitude() != null) {
            return new Coordinates(request.getLatitude(), request.getLongitude());
        }

        if (request.getLocationAddress() == null || request.getLocationAddress().isBlank()) {
            return new Coordinates(null, null);
        }

        String url = UriComponentsBuilder
                .fromUriString(NOMINATIM_URL)
                .queryParam("q", request.getLocationAddress())
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .queryParam("addressdetails", 0)
                .build()
                .toUriString();

        JsonNode response = restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.isArray() || response.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not resolve locationAddress. Try a more specific address");
        }

        JsonNode first = response.get(0);
        try {
            BigDecimal latitude = new BigDecimal(first.get("lat").asText());
            BigDecimal longitude = new BigDecimal(first.get("lon").asText());
            return new Coordinates(latitude, longitude);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Geocoding service returned invalid coordinates", ex);
        }
    }

    private record Coordinates(BigDecimal latitude, BigDecimal longitude) {}

    public String reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        String url = UriComponentsBuilder
                .fromUriString(NOMINATIM_REVERSE_URL)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("format", "json")
                .build()
                .toUriString();

        String rawResponse = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);

            if (root == null || root.get("display_name") == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Could not resolve coordinates to an address");
            }

            return root.get("display_name").asText();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to parse geocoding response", ex);
        }
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> {
                    PostResponse response = PostResponse.fromPost(post);
                    response.setLikeCount(likeRepository.countByPost(post));
                    response.setCommentCount(commentRepository.countByPost(post));
                    return response;
                })
                .toList();
    }

    public List<PostResponse> getPostsByUserId(UUID userId) {
        return postRepository.findByUser_Id(userId).stream()
                .map(post -> {
                    PostResponse response = PostResponse.fromPost(post);
                    response.setLikeCount(likeRepository.countByPost(post));
                    response.setCommentCount(commentRepository.countByPost(post));
                    return response;
                })
                .toList();
    }

    public PostResponse updateReportStatus(UUID postId, String status, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }

        post.setStatus(status);
        Post updatedPost = postRepository.save(post);
        return PostResponse.fromPost(updatedPost);
    }

    public PostResponse updateReportPriority(UUID postId, int priority, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (priority < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Priority must be non-negative");
        }

        post.setPriorityScore(priority);
        Post updatedPost = postRepository.save(post);
        return PostResponse.fromPost(updatedPost);
    }
    public void deletePost(UUID postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }

        postRepository.delete(post);
    }
    public Map<String, Object> getAllPostsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findAllPaged(pageable);

        List<PostResponse> content = postPage.getContent().stream()
                .map(post -> {
                    PostResponse response = PostResponse.fromPost(post);
                    response.setLikeCount(likeRepository.countByPost(post));
                    response.setCommentCount(commentRepository.countByPost(post));
                    return response;
                })
                .toList();

        return Map.of(
                "content", content,
                "page", postPage.getNumber(),
                "totalPages", postPage.getTotalPages(),
                "totalElements", postPage.getTotalElements(),
                "hasMore", !postPage.isLast()
        );
    }

    public PostResponse updatePost(UUID postId, Map<String, String> request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own posts");
        }

        String description = request.get("description");
        String imageUrl = request.get("imageUrl");

        if (description != null && !description.isBlank()) {
            post.setDescription(description);
        }

        if (imageUrl != null && !imageUrl.isBlank()) {
            post.setImageUrl(imageUrl);
            ClassificationResult result = classificationService.classifyFromUrl(imageUrl);
            post.setCategory(result.getCategory());
        }

        Post saved = postRepository.save(post);
        PostResponse response = PostResponse.fromPost(saved);
        response.setLikeCount(likeRepository.countByPost(saved));
        response.setCommentCount(commentRepository.countByPost(saved));
        return response;
    }
}