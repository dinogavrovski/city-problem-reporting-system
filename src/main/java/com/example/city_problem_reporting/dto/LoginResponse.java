package com.example.city_problem_reporting.dto;

import java.util.UUID;

public class LoginResponse {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;

    public LoginResponse(UUID id, String username, String email, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
}