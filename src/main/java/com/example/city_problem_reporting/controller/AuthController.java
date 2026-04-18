package com.example.city_problem_reporting.controller;

import com.example.city_problem_reporting.dto.LoginResponse;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.UserRepository;
import com.example.city_problem_reporting.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                token
        ));
    }
}