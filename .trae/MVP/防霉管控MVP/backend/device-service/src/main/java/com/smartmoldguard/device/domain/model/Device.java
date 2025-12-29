package com.smartmoldguard.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("devices")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status; // online, offline
    private String icon;
    private String location;
    private String macAddress;
    private String firmwareVersion;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
