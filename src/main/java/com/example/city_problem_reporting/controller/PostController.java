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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
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
}
