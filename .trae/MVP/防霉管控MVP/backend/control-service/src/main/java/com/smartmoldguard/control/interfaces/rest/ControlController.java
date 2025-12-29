package com.smartmoldguard.control.interfaces.rest;

import com.smartmoldguard.control.domain.model.InterventionPlan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class ControlController {

    @GetMapping("/active")
    public ResponseEntity<List<InterventionPlan>> getActivePlans() {
        // Mock response for now
        return ResponseEntity.ok(Collections.emptyList());
    }
}
