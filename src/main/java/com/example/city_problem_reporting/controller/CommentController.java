package com.example.city_problem_reporting.controller;

import com.example.city_problem_reporting.dto.CommentResponse;
import com.example.city_problem_reporting.dto.CreateCommentRequest;
import com.example.city_problem_reporting.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@SecurityRequirement(name = "basicAuth")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable UUID postId,
                                                      @RequestBody CreateCommentRequest request,
                                                      Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(postId, request, principal.getName()));
    }
}