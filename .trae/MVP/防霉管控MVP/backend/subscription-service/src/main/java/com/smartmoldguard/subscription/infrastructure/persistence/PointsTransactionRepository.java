package com.smartmoldguard.subscription.infrastructure.persistence;

import com.smartmoldguard.subscription.domain.model.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
