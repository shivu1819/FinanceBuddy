package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdOrSystemDefaultTrue(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    Optional<Category> findByName(String name);

    Optional<Category> findByUserIdAndName(Long userId, String name);
}
