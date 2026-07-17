package com.financebuddy.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "monthly_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_reports_user_month_year",
                columnNames = {"user_id", "report_month", "report_year"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_month", nullable = false)
    private Integer reportMonth;

    @Column(name = "report_year", nullable = false)
    private Integer reportYear;

    @Column(name = "total_income", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_expense", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Column(name = "net_savings", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netSavings = BigDecimal.ZERO;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
