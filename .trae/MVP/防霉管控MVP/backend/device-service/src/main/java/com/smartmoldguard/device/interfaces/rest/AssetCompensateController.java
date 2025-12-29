package com.smartmoldguard.device.interfaces.rest;

import com.smartmoldguard.device.application.service.AssetCompensateService;
import com.smartmoldguard.device.interfaces.rest.dto.CompensationConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/asset-compensate")
@RequiredArgsConstructor
public class AssetCompensateController {

    private final AssetCompensateService assetCompensateService;

    @PostMapping("/calculate")
    public ResponseEntity<BigDecimal> calculate(@RequestParam Long deviceId) {
        return ResponseEntity.ok(assetCompensateService.calculateCompensation(deviceId));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody CompensationConfirmRequest request) {
        assetCompensateService.confirmCompensation(request.getCompensationId());
        return ResponseEntity.ok().build();
    }
}
