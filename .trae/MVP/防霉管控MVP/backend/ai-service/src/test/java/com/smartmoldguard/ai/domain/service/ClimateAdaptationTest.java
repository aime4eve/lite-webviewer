package com.smartmoldguard.ai.domain.service;

import com.smartmoldguard.ai.domain.model.ClimateConfiguration;
import com.smartmoldguard.ai.domain.model.Microclimate;
import com.smartmoldguard.ai.domain.model.RiskAssessment;
import com.smartmoldguard.ai.infrastructure.persistence.ClimateConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClimateAdaptationTest {

    @Mock
    private ClimateConfigurationRepository repository;

    private ClimateConfigurationService configService;
    private RiskCalculator riskCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        configService = new ClimateConfigurationService(repository);
        riskCalculator = new RiskCalculator(configService);
    }

    @Test
    void testRiskCalculationWithDefaultConfig() {
        // Mock default config
        ClimateConfiguration defaultConfig = ClimateConfiguration.builder()
                .zoneCode("DEFAULT")
                .humidityThreshold(70.0)
                .tempThreshold(25.0)
                .riskFactorMultiplier(1.0)
                .build();
        
        when(repository.findByZoneCode("DEFAULT")).thenReturn(Optional.of(defaultConfig));
        when(repository.save(any(ClimateConfiguration.class))).thenReturn(defaultConfig);

        Microclimate microclimate = Microclimate.builder()
                .deviceId(1L)
                .temperature(26.0)
                .humidity(75.0) // > 70 but < 85 -> HIGH
                .timestamp(Instant.now())
                .build();

        RiskAssessment risk = riskCalculator.calculateRisk(microclimate, "Unknown");
        assertEquals("HIGH", risk.getRiskLevel());
        assertEquals(0.7, risk.getRiskScore());
    }

    @Test
    void testRiskCalculationWithCustomConfig() {
        // Mock Custom Config (e.g. Tropical)
        // Higher tolerance for humidity
        ClimateConfiguration tropicalConfig = ClimateConfiguration.builder()
                .zoneCode("CN-SOUTH")
                .humidityThreshold(80.0) // Higher threshold
                .tempThreshold(28.0)
                .riskFactorMultiplier(0.8) // Lower risk factor
                .build();

        when(repository.findByZoneCode("CN-SOUTH")).thenReturn(Optional.of(tropicalConfig));

        // Same microclimate: 26C, 75% Humidity
        // Default: HIGH risk (>70)
        // Tropical: Low risk (<80)
        
        Microclimate microclimate = Microclimate.builder()
                .deviceId(1L)
                .temperature(26.0)
                .humidity(75.0) 
                .timestamp(Instant.now())
                .build();

        // Simulate location mapping in service (mocking the service logic or repository return)
        // Since logic is in service method `getParametersForLocation`, let's mock repository call that happens inside.
        // My simple service logic maps "Guangdong" to "CN-SOUTH".
        
        RiskAssessment risk = riskCalculator.calculateRisk(microclimate, "Guangdong, Shenzhen");
        
        // 75 < 80 (Threshold) -> No HIGH/CRITICAL logic triggered
        // Temp 26 < 28 -> No MEDIUM logic triggered
        // Should be LOW
        
        assertEquals("LOW", risk.getRiskLevel());
        assertEquals(0.0, risk.getRiskScore());
    }
}
