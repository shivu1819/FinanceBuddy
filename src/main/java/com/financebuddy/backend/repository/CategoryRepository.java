package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdOrSystemDefaultTrue(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    Optional<Category> findByName(String name);

    Optional<Category> findByUserIdAndName(Long userId, String name);

    @Query("""
            SELECT COUNT(c)
            FROM Category c
            WHERE c.user.id = :userId
            """)
    long countCategoriesByUserId(@Param("userId") Long userId);
}
