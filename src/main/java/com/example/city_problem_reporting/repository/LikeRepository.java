package com.example.city_problem_reporting.repository;

import com.example.city_problem_reporting.model.Like;
import com.example.city_problem_reporting.model.Post;
import com.example.city_problem_reporting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    int countByPost(Post post);
    boolean existsByPostAndUser(Post post, User user);
    Optional<Like> findByPostAndUser(Post post, User user);
}