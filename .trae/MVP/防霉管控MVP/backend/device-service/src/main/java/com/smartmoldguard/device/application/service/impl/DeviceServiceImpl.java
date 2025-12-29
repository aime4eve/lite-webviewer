package com.smartmoldguard.device.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmoldguard.device.application.service.DeviceService;
import com.smartmoldguard.device.domain.model.ButtonMapping;
import com.smartmoldguard.device.domain.model.Device;
import com.smartmoldguard.device.interfaces.rest.dto.ButtonMappingUpdateRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceBindRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceRegisterRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceTelemetryRequest;
import com.smartmoldguard.device.mapper.ButtonMappingMapper;
import com.smartmoldguard.device.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartmoldguard.device.domain.event.DeviceTamperedEvent;
import com.smartmoldguard.device.domain.event.DeviceTelemetryEvent;
import org.springframework.kafka.core.KafkaTemplate;
import com.smartmoldguard.device.mapper.WorkOrderMapper;
import com.smartmoldguard.device.mapper.AssetCompensateMapper;
import com.smartmoldguard.device.domain.model.WorkOrder;
import com.smartmoldguard.device.domain.model.AssetCompensate;
import java.math.BigDecimal;
import com.smartmoldguard.device.domain.model.Alarm;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final ButtonMappingMapper buttonMappingMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WorkOrderMapper workOrderMapper;
    private final AssetCompensateMapper assetCompensateMapper;
    private final com.smartmoldguard.device.mapper.AlarmMapper alarmMapper;

    @Override
    @Transactional
    public Device register(DeviceRegisterRequest request) {   // Check if SN exists
        if (count(new LambdaQueryWrapper<Device>().eq(Device::getMacAddress, request.getSn())) > 0) {
             throw new RuntimeException("Device with SN " + request.getSn() + " already exists");
        }

        Device device = new Device();
        device.setName(request.getName());
        device.setMacAddress(request.getSn()); // Using SN as MacAddress/Identifier for now
        device.setLocation(request.getLocation());
        device.setDescription(request.getDescription());
        device.setStatus("pending");
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        
        save(device);
        return device;
    }

    @Override
    @Transactional
    public Device bind(Long deviceId, DeviceBindRequest request) {
        Device device = getById(deviceId);
        if (device == null) {
            throw new RuntimeException("Device not found");
        }
        
        // Logic for binding (e.g. associating with user, changing status)
        device.setName(request.getName());
        device.setStatus("online"); // Simulated online after bind
        device.setUpdatedAt(LocalDateTime.now());
        
        updateById(device);
        return device;
    }

    @Override
    @Transactional
    public void updateButtonMappings(Long deviceId, ButtonMappingUpdateRequest request) {
        // Clear existing mappings
        buttonMappingMapper.delete(new LambdaQueryWrapper<ButtonMapping>().eq(ButtonMapping::getDeviceId, deviceId));

        // Add new mappings
        List<ButtonMapping> mappings = request.getMappings().stream().map(dto -> {
            ButtonMapping mapping = new ButtonMapping();
            mapping.setDeviceId(deviceId);
            mapping.setSwitchPosition(dto.getSwitchPosition());
            mapping.setDeviceType(dto.getDeviceType());
            mapping.setDeviceName(dto.getDeviceName());
            mapping.setIcon(dto.getIcon());
            return mapping;
        }).collect(Collectors.toList());

        for (ButtonMapping mapping : mappings) {
            buttonMappingMapper.insert(mapping);
        }
    }

    @Override
    @Transactional
    public void handleTamperAlert(Long deviceId) {
        Device device = getById(deviceId);
        if (device == null) {
            throw new RuntimeException("Device not found");
        }

        // 更新设备状态为异常
        device.setStatus("abnormal");
        device.setUpdatedAt(LocalDateTime.now());
        updateById(device);

        // 发送 Kafka 事件
        DeviceTamperedEvent event = new DeviceTamperedEvent(
                device.getId(),
                device.getMacAddress(),
                device.getLocation(),
                LocalDateTime.now(),
                "tamper",
                "unhandled"
        );
        try {
            kafkaTemplate.send("device-tampered-topic", event).get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            // Log error but don't fail transaction if Kafka is down? 
            // Or better, rethrow to ensure data consistency?
            // For now, let's log and rethrow to see the error in logs.
            System.err.println("Failed to send Kafka event: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send Kafka event", e);
        }

        // Create Alarm
        Alarm alarm = new Alarm();
        alarm.setDeviceId(device.getId());
        alarm.setSeverity("HIGH");
        alarm.setMessage("设备防拆告警: " + device.getLocation());
        alarm.setStatus("ACTIVE");
        alarm.setTimestamp(LocalDateTime.now());
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());
        alarmMapper.insert(alarm);
        
        // 触发运维工单创建
        createTamperWorkOrder(device);
    }

    private void createTamperWorkOrder(Device device) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setDeviceId(device.getId());
        workOrder.setType("tamper");
        workOrder.setStatus("pending");
        workOrder.setDescription("设备防拆告警: " + device.getLocation());
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.insert(workOrder);

        // 创建初始赔付记录
        AssetCompensate compensate = new AssetCompensate();
        compensate.setDeviceId(device.getId());
        compensate.setWorkOrderId(workOrder.getId());
        compensate.setAmount(new BigDecimal("50.00")); // 默认赔付金额，后续可调用计算服务更新
        compensate.setStatus("pending");
        compensate.setCreatedAt(LocalDateTime.now());
        assetCompensateMapper.insert(compensate);
    }

    @Override
    public void processTelemetry(Long deviceId, DeviceTelemetryRequest request) {
        Device device = getById(deviceId);
        if (device == null) {
            throw new RuntimeException("Device not found");
        }

        // Publish to Kafka
        DeviceTelemetryEvent event = new DeviceTelemetryEvent(
                device.getId(),
                request.getTemperature(),
                request.getHumidity(),
                device.getLocation(),
                LocalDateTime.now()
        );

        try {
            kafkaTemplate.send("device-telemetry-topic", event);
        } catch (Exception e) {
            System.err.println("Failed to send telemetry event: " + e.getMessage());
        }
    }
}
