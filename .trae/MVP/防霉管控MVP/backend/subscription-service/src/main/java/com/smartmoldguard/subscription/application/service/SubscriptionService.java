package com.smartmoldguard.subscription.application.service;

import com.smartmoldguard.subscription.domain.event.SubscriptionExpiredEvent;
import com.smartmoldguard.subscription.domain.model.LoyaltyPoints;
import com.smartmoldguard.subscription.domain.model.Subscription;
import com.smartmoldguard.subscription.infrastructure.persistence.LoyaltyPointsRepository;
import com.smartmoldguard.subscription.domain.model.PointsTransaction;
import com.smartmoldguard.subscription.infrastructure.persistence.PointsTransactionRepository;
import com.smartmoldguard.subscription.infrastructure.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final LoyaltyPointsRepository loyaltyPointsRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @SuppressWarnings("null")
    public Subscription createSubscription(Long userId, String planName) {
        // Deactivate existing active subscription
        subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .ifPresent(sub -> {
                    sub.setStatus("UPGRADED");
                    sub.setEndDate(LocalDateTime.now());
                    subscriptionRepository.save(sub);
                });

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .planName(planName)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1)) // Default 1 month
                .build();

        return Objects.requireNonNull(subscriptionRepository.save(subscription), "保存订阅失败");
    }

    public Subscription getActiveSubscription(Long userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
    }

    @Transactional
    @SuppressWarnings("null")
    public LoyaltyPoints addPoints(Long userId, Integer points, String description) {
        LoyaltyPoints lp = loyaltyPointsRepository.findByUserId(userId)
                .orElse(LoyaltyPoints.builder()
                        .userId(userId)
                        .points(0)
                        .totalEarned(0)
                        .build());
        
        lp.setPoints(lp.getPoints() + points);
        lp.setTotalEarned(lp.getTotalEarned() + points);
        
        LoyaltyPoints saved = Objects.requireNonNull(loyaltyPointsRepository.save(lp), "保存积分失败");

        // Record Transaction
        PointsTransaction tx = PointsTransaction.builder()
                .userId(userId)
                .amount(points)
                .type(points > 0 ? "EARN" : "SPEND")
                .description(description)
                .build();
        pointsTransactionRepository.save(Objects.requireNonNull(tx, "积分流水不能为空"));
        
        return saved;
    }

    // Overload for backward compatibility
    @Transactional
    public LoyaltyPoints addPoints(Long userId, Integer points) {
        return addPoints(userId, points, "General Adjustment");
    }

    public java.util.List<PointsTransaction> getPointsHistory(Long userId) {
        return pointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public LoyaltyPoints getPoints(Long userId) {
        return loyaltyPointsRepository.findByUserId(userId)
                .orElse(LoyaltyPoints.builder()
                        .userId(userId)
                        .points(0)
                        .totalEarned(0)
                        .build());
    }
    
    @SuppressWarnings("null")
    public void checkExpiration(Long subscriptionId) {
        Long id = Objects.requireNonNull(subscriptionId, "订阅ID不能为空");
        subscriptionRepository.findById(id).ifPresent(sub -> {
            if (sub.getEndDate().isBefore(LocalDateTime.now()) && "ACTIVE".equals(sub.getStatus())) {
                sub.setStatus("EXPIRED");
                subscriptionRepository.save(sub);

                SubscriptionExpiredEvent event = SubscriptionExpiredEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .subscriptionId(sub.getId())
                        .userId(sub.getUserId())
                        .expiredAt(Instant.now())
                        .build();

                kafkaTemplate.send("subscription-expired-topic", event);
                log.info("Subscription expired for user {}", sub.getUserId());
            }
        });
    }
}
