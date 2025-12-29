package com.smartmoldguard.device.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 设备配网接口
 * 负责设备发现与密钥交换
 */
@RestController
@RequestMapping("/api/v1/provisioning")
@Tag(name = "Provisioning", description = "设备配网接口")
public class ProvisioningController {

    /**
     * 获取配网信息
     * @return 配网信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取配网信息")
    public ResponseEntity<ProvisioningInfo> getProvisioningInfo() {
        ProvisioningInfo info = new ProvisioningInfo();
        info.setSsidPrefix("SmartMoldGuard_");
        info.setProtocolVersion("1.0");
        info.setDiscoveryPort(1900);
        return ResponseEntity.ok(info);
    }

    /**
     * 密钥交换
     * @param request 密钥交换请求
     * @return 密钥交换响应
     */
    @PostMapping("/exchange-keys")
    @Operation(summary = "密钥交换")
    public ResponseEntity<KeyExchangeResponse> exchangeKeys(@RequestBody KeyExchangeRequest request) {
        // Mock Key Exchange (Diffie-Hellman or similar would go here)
        // Receiving device public key, returning server public key and session token
        
        KeyExchangeResponse response = new KeyExchangeResponse();
        response.setServerPublicKey("MOCK_SERVER_PUBLIC_KEY_" + UUID.randomUUID().toString());
        response.setSessionToken(UUID.randomUUID().toString());
        response.setExpiresIn(3600);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 配网信息 DTO
     */
    @Data
    public static class ProvisioningInfo {
        /**
         * SSID 前缀
         */
        private String ssidPrefix;
        /**
         * 协议版本
         */
        private String protocolVersion;
        /**
         * 发现端口
         */
        private int discoveryPort;
    }

    /**
     * 密钥交换请求 DTO
     */
    @Data
    public static class KeyExchangeRequest {
        /**
         * 设备公钥
         */
        private String devicePublicKey;
        /**
         * 设备序列号
         */
        private String deviceSn;
    }

    /**
     * 密钥交换响应 DTO
     */
    @Data
    public static class KeyExchangeResponse {
        /**
         * 服务端公钥
         */
        private String serverPublicKey;
        /**
         * 会话令牌
         */
        private String sessionToken;
        /**
         * 过期时间 (秒)
         */
        private int expiresIn;
    }
}
