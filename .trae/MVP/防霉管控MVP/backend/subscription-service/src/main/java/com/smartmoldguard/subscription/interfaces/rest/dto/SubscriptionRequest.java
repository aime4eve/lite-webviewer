package com.smartmoldguard.subscription.interfaces.rest.dto;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Long userId;
    private String planName;
}
