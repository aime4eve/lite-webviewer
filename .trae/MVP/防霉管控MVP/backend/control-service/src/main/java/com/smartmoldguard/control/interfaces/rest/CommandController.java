package com.smartmoldguard.control.interfaces.rest;

import com.smartmoldguard.control.domain.event.ControlCommandEvent;
import com.smartmoldguard.control.infrastructure.messaging.KafkaProducerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/control/commands")
@RequiredArgsConstructor
public class CommandController {

    private final KafkaProducerService producerService;
    // In-memory history for demo/MVP
    private final Map<Long, List<ControlCommandEvent>> commandHistory = new ConcurrentHashMap<>();

    @PostMapping("/send")
    public ResponseEntity<CommandResponse> sendCommand(@RequestBody CommandRequest request) {
        ControlCommandEvent event = ControlCommandEvent.builder()
                .commandId(UUID.randomUUID().toString())
                .deviceId(request.getDeviceId())
                .commandType(request.getCommandType())
                .reason("Manual Control")
                .timestamp(Instant.now())
                .build();

        producerService.sendControlCommand(event);
        
        commandHistory.computeIfAbsent(request.getDeviceId(), k -> new ArrayList<>()).add(event);

        return ResponseEntity.ok(new CommandResponse(request.getDeviceId(), request.getCommandType(), "SENT"));
    }

    @GetMapping("/history/{deviceId}")
    public ResponseEntity<List<ControlCommandEvent>> getHistory(@PathVariable Long deviceId) {
        return ResponseEntity.ok(commandHistory.getOrDefault(deviceId, List.of()));
    }

    @Data
    public static class CommandRequest {
        private Long deviceId;
        private String commandType;
        private Map<String, Object> parameters;
    }

    @Data
    public static class CommandResponse {
        private Long deviceId;
        private String commandType;
        private String status;
        
        public CommandResponse(Long deviceId, String commandType, String status) {
            this.deviceId = deviceId;
            this.commandType = commandType;
            this.status = status;
        }
    }
}
