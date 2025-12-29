package com.smartmoldguard.subscription.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionExpiredEvent {
    private String eventId;
    private Long subscriptionId;
    private Long userId;
    private Instant expiredAt;
}
