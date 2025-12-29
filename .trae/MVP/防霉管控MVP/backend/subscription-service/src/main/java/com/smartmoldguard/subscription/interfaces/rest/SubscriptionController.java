package com.smartmoldguard.subscription.interfaces.rest;

import com.smartmoldguard.subscription.application.service.SubscriptionService;
import com.smartmoldguard.subscription.domain.model.LoyaltyPoints;
import com.smartmoldguard.subscription.domain.model.PointsTransaction;
import com.smartmoldguard.subscription.domain.model.Subscription;
import com.smartmoldguard.subscription.interfaces.rest.dto.PointsRequest;
import com.smartmoldguard.subscription.interfaces.rest.dto.SubscriptionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscription(request.getUserId(), request.getPlanName()));
    }

    @GetMapping("/{userId}/active")
    public ResponseEntity<Subscription> getActiveSubscription(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getActiveSubscription(userId));
    }

    @PostMapping("/points/add")
    public ResponseEntity<LoyaltyPoints> addPoints(@RequestBody PointsRequest request) {
        return ResponseEntity.ok(subscriptionService.addPoints(request.getUserId(), request.getPoints()));
    }

    @GetMapping("/{userId}/points")
    public ResponseEntity<LoyaltyPoints> getPoints(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getPoints(userId));
    }

    @GetMapping("/{userId}/points/history")
    public ResponseEntity<java.util.List<PointsTransaction>> getPointsHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getPointsHistory(userId));
    }
}
