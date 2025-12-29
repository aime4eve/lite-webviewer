package com.smartmoldguard.device.interfaces.rest;

import com.smartmoldguard.device.application.service.DeviceService;
import com.smartmoldguard.device.domain.model.Device;
import com.smartmoldguard.device.interfaces.rest.dto.ButtonMappingUpdateRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceBindRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceRegisterRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceTelemetryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<Device> register(@RequestBody DeviceRegisterRequest request) {
        return ResponseEntity.ok(deviceService.register(request));
    }

    @PostMapping("/{deviceId}/bind")
    public ResponseEntity<Device> bind(@PathVariable Long deviceId, @RequestBody DeviceBindRequest request) {
        return ResponseEntity.ok(deviceService.bind(deviceId, request));
    }

    @PutMapping("/{deviceId}/button-mapping")
    public ResponseEntity<Void> updateButtonMapping(@PathVariable Long deviceId, @RequestBody ButtonMappingUpdateRequest request) {
        deviceService.updateButtonMappings(deviceId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 模拟触发设备防拆告警
     * @param deviceId 设备ID
     * @return Void
     */
    @PostMapping("/{deviceId}/simulate-tamper")
    public ResponseEntity<Void> simulateTamper(@PathVariable Long deviceId) {
        deviceService.handleTamperAlert(deviceId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deviceId}/telemetry")
    public ResponseEntity<Void> reportTelemetry(@PathVariable Long deviceId, @RequestBody DeviceTelemetryRequest request) {
        deviceService.processTelemetry(deviceId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<java.util.List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.list());
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long deviceId) {
        return ResponseEntity.ok(deviceService.getById(deviceId));
    }

    @PutMapping("/{deviceId}")
    public ResponseEntity<Void> updateDevice(@PathVariable Long deviceId, @RequestBody Device device) {
        device.setId(deviceId);
        deviceService.updateById(device);
        return ResponseEntity.ok().build();
    }
}
