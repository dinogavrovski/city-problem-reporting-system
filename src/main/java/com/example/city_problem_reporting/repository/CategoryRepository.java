package com.example.city_problem_reporting.repository;

import com.example.city_problem_reporting.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
