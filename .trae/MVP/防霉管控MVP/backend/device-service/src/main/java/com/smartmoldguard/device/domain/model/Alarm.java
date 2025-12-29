package com.smartmoldguard.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警实体
 */
@Data
@TableName("alarms")
public class Alarm {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 严重程度 (HIGH, MEDIUM, LOW)
     */
    private String severity;
    
    /**
     * 告警消息
     */
    private String message;
    
    /**
     * 状态 (ACTIVE, CLEARED, CONFIRMED)
     */
    private String status;
    
    /**
     * 告警发生时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
