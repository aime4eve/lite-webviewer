package com.smartmoldguard.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运维工单实体
 */
@Data
@TableName("work_orders")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String type; // "tamper", "repair", "cleaning"
    private String status; // "pending", "closed", "assigned"
    private String assignee; // User ID or Name
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
