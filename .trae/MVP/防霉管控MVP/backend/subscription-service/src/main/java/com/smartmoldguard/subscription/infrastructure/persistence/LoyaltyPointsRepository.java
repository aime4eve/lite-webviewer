package com.smartmoldguard.subscription.infrastructure.persistence;

import com.smartmoldguard.subscription.domain.model.LoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Long> {
    Optional<LoyaltyPoints> findByUserId(Long userId);
}
