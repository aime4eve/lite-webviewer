package com.smartmoldguard.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("button_mappings")
public class ButtonMapping {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Integer switchPosition; // 1, 2, 3
    private String deviceType; // fan, heater, light
    private String deviceName;
    private String icon;
}
