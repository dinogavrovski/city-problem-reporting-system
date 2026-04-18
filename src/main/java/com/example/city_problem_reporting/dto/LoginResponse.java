package com.example.city_problem_reporting.dto;

import java.util.UUID;

public class LoginResponse {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private String token;

    public LoginResponse(UUID id, String username, String email, String avatarUrl, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.token = token;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getToken() { return token; }
}