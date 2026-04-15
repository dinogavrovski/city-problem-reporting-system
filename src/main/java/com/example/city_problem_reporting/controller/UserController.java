package com.example.city_problem_reporting.controller;

import com.example.city_problem_reporting.dto.CreateUserRequest;
import com.example.city_problem_reporting.dto.UpdateUserRequest;
import com.example.city_problem_reporting.dto.UserResponse;
import com.example.city_problem_reporting.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Principal principal) {
        return ResponseEntity.ok(userService.getUserByUsername(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@RequestBody UpdateUserRequest request,
                                                 Principal principal) {
        return ResponseEntity.ok(userService.updateUser(principal.getName(), request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
}