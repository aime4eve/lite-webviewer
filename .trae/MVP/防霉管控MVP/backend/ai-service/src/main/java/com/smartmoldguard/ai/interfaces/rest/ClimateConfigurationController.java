package com.smartmoldguard.ai.interfaces.rest;

import com.smartmoldguard.ai.domain.model.ClimateConfiguration;
import com.smartmoldguard.ai.domain.service.ClimateConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/climate-configs")
@RequiredArgsConstructor
@Tag(name = "Climate Configuration", description = "气候带算法参数配置接口")
public class ClimateConfigurationController {

    private final ClimateConfigurationService service;

    @GetMapping
    @Operation(summary = "获取所有气候带配置")
    public ResponseEntity<List<ClimateConfiguration>> getAll() {
        return ResponseEntity.ok(service.getAllConfigurations());
    }

    @GetMapping("/{zoneCode}")
    @Operation(summary = "根据代码获取配置")
    public ResponseEntity<ClimateConfiguration> getByZoneCode(@PathVariable String zoneCode) {
        return service.getConfiguration(zoneCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建或更新配置")
    public ResponseEntity<ClimateConfiguration> save(@RequestBody ClimateConfiguration config) {
        return ResponseEntity.ok(service.saveConfiguration(config));
    }

    @DeleteMapping("/{zoneCode}")
    @Operation(summary = "删除配置")
    public ResponseEntity<Void> delete(@PathVariable String zoneCode) {
        service.deleteConfiguration(zoneCode);
        return ResponseEntity.noContent().build();
    }
}
