package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;

/**
 * 工单转发请求
 */
@Data
public class WorkOrderForwardRequest {
    /**
     * 转发给谁
     */
    private String toUser;
    /**
     * 转发原因
     */
    private String reason;
}
