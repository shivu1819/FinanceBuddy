package com.financebuddy.backend.billsplit;

import com.financebuddy.backend.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillSplitRepository extends JpaRepository<BillSplit, Long> {

    List<BillSplit> findByUserOrderByCreatedAtDesc(User user);

    Optional<BillSplit> findByIdAndUser(Long id, User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BillSplit b WHERE b.id = :id AND b.user = :user")
    Optional<BillSplit> findForUpdateByIdAndUser(@Param("id") Long id, @Param("user") User user);
}
