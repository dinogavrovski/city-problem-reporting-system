package com.example.city_problem_reporting.repository;

import com.example.city_problem_reporting.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUser_Id(UUID userId);

    @Query("""
        SELECT p FROM Post p
        WHERE (:category IS NULL OR p.category.name = :category)
        AND (:status IS NULL OR p.status = :status)
        AND (:startDate IS NULL OR p.createdAt >= :startDate)
        AND (:endDate IS NULL OR p.createdAt <= :endDate)
    """)
    Page<Post> findWithFilters(String category,
                               String status,
                               LocalDateTime startDate,
                               LocalDateTime endDate,
                               Pageable pageable);
}

