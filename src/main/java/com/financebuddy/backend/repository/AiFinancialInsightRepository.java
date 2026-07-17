package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.AiFinancialInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiFinancialInsightRepository extends JpaRepository<AiFinancialInsight, Long> {

    List<AiFinancialInsight> findByUserId(Long userId);

    List<AiFinancialInsight> findByUserIdAndInsightType(Long userId, String insightType);

    List<AiFinancialInsight> findByUserIdAndSeverity(Long userId, String severity);
}
