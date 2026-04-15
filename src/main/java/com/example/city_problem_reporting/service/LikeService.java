package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.dto.LikeResponse;
import com.example.city_problem_reporting.model.Like;
import com.example.city_problem_reporting.model.Post;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.LikeRepository;
import com.example.city_problem_reporting.repository.PostRepository;
import com.example.city_problem_reporting.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository,
                       PostRepository postRepository,
                       UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public LikeResponse likePost(UUID postId, String username) {
        Post post = getPost(postId);
        User user = getUser(username);

        if (!likeRepository.existsByPostAndUser(post, user)) {
            Like like = new Like();
            like.setPost(post);
            like.setUser(user);
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);
        }

        return new LikeResponse(likeRepository.countByPost(post), true);
    }

    public LikeResponse unlikePost(UUID postId, String username) {
        Post post = getPost(postId);
        User user = getUser(username);

        likeRepository.findByPostAndUser(post, user)
                .ifPresent(likeRepository::delete);

        return new LikeResponse(likeRepository.countByPost(post), false);
    }

    public LikeResponse getLikes(UUID postId, String username) {
        Post post = getPost(postId);
        User user = getUser(username);
        int count = likeRepository.countByPost(post);
        boolean liked = likeRepository.existsByPostAndUser(post, user);
        return new LikeResponse(count, liked);
    }

    private Post getPost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}