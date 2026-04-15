package com.example.city_problem_reporting.repository;

import com.example.city_problem_reporting.model.Comment;
import com.example.city_problem_reporting.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    int countByPost(Post post);
}