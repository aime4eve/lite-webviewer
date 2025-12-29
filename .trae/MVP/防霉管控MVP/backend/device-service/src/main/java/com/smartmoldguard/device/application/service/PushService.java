package com.smartmoldguard.device.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息推送服务
 */
@Service
@Slf4j
public class PushService {

    /**
     * 推送通知
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     */
    public void pushNotification(Long userId, String title, String content) {
        // Mock Push Notification (e.g. UniPush / WebSocket)
        log.info("PUSH to User[{}]: {} - {}", userId, title, content);
    }

    /**
     * 推送给设备拥有者
     * @param deviceId 设备ID
     * @param title 标题
     * @param content 内容
     */
    public void pushToDeviceOwner(Long deviceId, String title, String content) {
        // Mock logic to find owner
        Long mockOwnerId = 999L;
        pushNotification(mockOwnerId, title, content);
    }
}
