package com.smartmoldguard.device.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmoldguard.device.domain.model.Device;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceRegisterRequest;
import com.smartmoldguard.device.interfaces.rest.dto.DeviceBindRequest;

public interface DeviceService extends IService<Device> {
    Device register(DeviceRegisterRequest request);
    Device bind(Long deviceId, DeviceBindRequest request);
    void updateButtonMappings(Long deviceId, com.smartmoldguard.device.interfaces.rest.dto.ButtonMappingUpdateRequest request);
    
    /**
     * 处理设备防拆告警
     * @param deviceId 设备ID
     */
    void handleTamperAlert(Long deviceId);

    /**
     * 处理设备遥测数据
     * @param deviceId 设备ID
     * @param request 遥测数据
     */
    void processTelemetry(Long deviceId, com.smartmoldguard.device.interfaces.rest.dto.DeviceTelemetryRequest request);
}
