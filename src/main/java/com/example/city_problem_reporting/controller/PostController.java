package com.example.city_problem_reporting.controller;

import com.example.city_problem_reporting.dto.CreatePostRequest;
import com.example.city_problem_reporting.dto.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.example.city_problem_reporting.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping
    @Operation(summary = "Get all posts", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping("/create")
    @Operation(summary = "Create post", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request, Principal principal) {
        PostResponse createdPost = postService.createPost(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping("/geocode/reverse")
    public ResponseEntity<Map<String, String>> reverseGeocode(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng) {

        String address = postService.reverseGeocode(lat, lng);
        return ResponseEntity.ok(Map.of("address", address));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all posts by user", security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<List<PostResponse>> getPostsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @PutMapping("/{postId}/status")
    @Operation(summary = "Update report status", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PostResponse> updateReportStatus(
            @PathVariable UUID postId,
            @RequestParam String status,
            Principal principal) {

        PostResponse updatedPost = postService.updateReportStatus(postId, status, principal.getName());
        return ResponseEntity.ok(updatedPost);
    }



}
