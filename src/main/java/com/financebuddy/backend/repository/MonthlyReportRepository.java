package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    List<MonthlyReport> findByUserId(Long userId);

    Optional<MonthlyReport> findByUserIdAndReportMonthAndReportYear(Long userId, Integer reportMonth, Integer reportYear);

    List<MonthlyReport> findByUserIdAndReportYear(Long userId, Integer reportYear);
}
