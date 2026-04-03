package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.CreateUserRequest;
import com.example.city_problem_reporting.dto.UserResponse;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAvatarUrl(request.getAvatarUrl());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setUsername(savedUser.getUsername());
        response.setAvatarUrl(savedUser.getAvatarUrl());
        response.setCreatedAt(savedUser.getCreatedAt());

        return response;
    }
}
