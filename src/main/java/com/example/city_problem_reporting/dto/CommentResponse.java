package com.example.city_problem_reporting.dto;

import com.example.city_problem_reporting.model.Comment;
import java.time.LocalDateTime;
import java.util.UUID;

public class CommentResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static CommentResponse fromComment(Comment comment) {
        CommentResponse r = new CommentResponse();
        r.setId(comment.getId());
        r.setUserId(comment.getUser().getId());
        r.setUsername(comment.getUser().getUsername());
        r.setAvatarUrl(comment.getUser().getAvatarUrl());
        r.setContent(comment.getContent());
        r.setCreatedAt(comment.getCreatedAt());
        return r;
    }
}