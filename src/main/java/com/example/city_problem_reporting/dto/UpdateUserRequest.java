package com.example.city_problem_reporting.dto;

public class UpdateUserRequest {
    private String username;
    private String avatarUrl;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}