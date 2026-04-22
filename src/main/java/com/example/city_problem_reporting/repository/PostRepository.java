package com.example.city_problem_reporting.repository;

import com.example.city_problem_reporting.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<Post> findAllPaged(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId")
    List<Post> findByUser_Id(UUID userId);
}