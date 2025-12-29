package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;

@Data
public class CompensationConfirmRequest {
    private Long compensationId;
    private String paymentMethod; // wechat, alipay
}
