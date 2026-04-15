package com.example.city_problem_reporting.controller;

import com.example.city_problem_reporting.dto.LikeResponse;
import com.example.city_problem_reporting.service.LikeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@SecurityRequirement(name = "basicAuth")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping
    public ResponseEntity<LikeResponse> getLikes(@PathVariable UUID postId, Principal principal) {
        return ResponseEntity.ok(likeService.getLikes(postId, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<LikeResponse> like(@PathVariable UUID postId, Principal principal) {
        return ResponseEntity.ok(likeService.likePost(postId, principal.getName()));
    }

    @DeleteMapping
    public ResponseEntity<LikeResponse> unlike(@PathVariable UUID postId, Principal principal) {
        return ResponseEntity.ok(likeService.unlikePost(postId, principal.getName()));
    }
}