package com.smartmoldguard.device.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmoldguard.device.domain.model.AssetCompensate;
import java.math.BigDecimal;

public interface AssetCompensateService extends IService<AssetCompensate> {
    /**
     * 计算赔付金额
     * @param deviceId 设备ID
     * @return 赔付金额
     */
    BigDecimal calculateCompensation(Long deviceId);

    /**
     * 确认赔付
     * @param compensationId 赔付记录ID
     */
    void confirmCompensation(Long compensationId);
}
