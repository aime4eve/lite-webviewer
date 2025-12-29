package com.smartmoldguard.subscription.infrastructure.persistence;

import com.smartmoldguard.subscription.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, String status);
    List<Subscription> findByUserId(Long userId);
}
