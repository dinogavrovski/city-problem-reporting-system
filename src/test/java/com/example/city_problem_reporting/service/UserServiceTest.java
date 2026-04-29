package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.CreateUserRequest;
import com.example.city_problem_reporting.dto.UpdateUserRequest;
import com.example.city_problem_reporting.dto.UserResponse;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateUserRequest();
        validRequest.setEmail("ana@test.com");
        validRequest.setUsername("ana");
        validRequest.setPassword("secret123");
        validRequest.setAvatarUrl("http://img/a.jpg");
    }

    @Test
    void createUser_withValidRequest_savesAndReturnsResponse() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("ana")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = userService.createUser(validRequest);

        assertThat(response.getEmail()).isEqualTo("ana@test.com");
        assertThat(response.getUsername()).isEqualTo("ana");
        assertThat(response.getId()).isNotNull();
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void createUser_blankEmail_throwsBadRequest() {
        validRequest.setEmail("");
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("email");
    }

    @Test
    void createUser_blankUsername_throwsBadRequest() {
        validRequest.setUsername("   ");
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("username");
    }

    @Test
    void createUser_blankPassword_throwsBadRequest() {
        validRequest.setPassword(null);
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("password");
    }

    @Test
    void createUser_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("email already exists");
    }

    @Test
    void createUser_duplicateUsername_throwsConflict() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("ana")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("username already exists");
    }

    @Test
    void getUserByUsername_whenExists_returnsResponse() {
        User user = sampleUser();
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));
        UserResponse response = userService.getUserByUsername("ana");
        assertThat(response.getUsername()).isEqualTo("ana");
    }

    @Test
    void getUserByUsername_whenMissing_throwsNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserByUsername("ghost"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_changesUsernameAndAvatar() {
        User existing = sampleUser();
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("ana_new")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("ana_new");
        req.setAvatarUrl("http://img/new.jpg");

        UserResponse response = userService.updateUser("ana", req);
        assertThat(response.getUsername()).isEqualTo("ana_new");
        assertThat(response.getAvatarUrl()).isEqualTo("http://img/new.jpg");
    }

    @Test
    void updateUser_newUsernameAlreadyTaken_throwsConflict() {
        User existing = sampleUser();
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("taken");

        assertThatThrownBy(() -> userService.updateUser("ana", req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Username already taken");
    }

    private User sampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("ana@test.com");
        user.setUsername("ana");
        user.setPasswordHash("$2a$10$hash");
        user.setAvatarUrl("http://img/a.jpg");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}