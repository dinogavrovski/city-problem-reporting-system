package com.example.city_problem_reporting.dto;

import com.example.city_problem_reporting.model.Post;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PostResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String avatarUrl;
    private String description;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String category;
    private Integer priorityScore;
    private String status;
    private LocalDateTime createdAt;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;

    // --- Getters & Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }

    public static PostResponse fromPost(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUser().getId());
        response.setUsername(post.getUser().getUsername());
        response.setAvatarUrl(post.getUser().getAvatarUrl());
        response.setDescription(post.getDescription());
        response.setImageUrl(post.getImageUrl());
        response.setLatitude(post.getLatitude());
        response.setLongitude(post.getLongitude());
        response.setCategory(post.getCategory());
        response.setPriorityScore(post.getPriorityScore());
        response.setStatus(post.getStatus());
        response.setCreatedAt(post.getCreatedAt());
        // likeCount, commentCount, likedByMe set separately by service
        return response;
    }
}