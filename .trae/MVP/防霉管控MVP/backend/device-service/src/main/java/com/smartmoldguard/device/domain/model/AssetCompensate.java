package com.smartmoldguard.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产赔付实体
 */
@Data
@TableName("asset_compensations")
public class AssetCompensate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long workOrderId;
    private BigDecimal amount;
    private String status; // "pending", "paid"
    private LocalDateTime createdAt;
}
