package com.smartmoldguard.control.infrastructure.messaging;

import com.smartmoldguard.control.domain.event.ControlCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "control-command-topic";

    @SuppressWarnings("null")
    public void sendControlCommand(ControlCommandEvent command) {
        log.info("Sending control command: {}", command);
        Long deviceId = Objects.requireNonNull(command.getDeviceId(), "deviceId 不能为空");
        kafkaTemplate.send(TOPIC, deviceId.toString(), command);
    }
}
