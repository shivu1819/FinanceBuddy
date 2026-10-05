package com.financebuddy.backend.investment;

import com.financebuddy.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserOrderByInvestmentDateDescCreatedAtDesc(User user);

    Optional<Investment> findByIdAndUser(Long id, User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Investment i WHERE i.id = :id AND i.user = :user")
    Optional<Investment> findForUpdateByIdAndUser(@Param("id") Long id, @Param("user") User user);
}
