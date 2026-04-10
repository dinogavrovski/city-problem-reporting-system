package com.example.city_problem_reporting.repository;

import com.example.city_problem_reporting.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUser_Id(UUID userId);
}
