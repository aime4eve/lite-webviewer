package com.smartmoldguard.device.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmoldguard.device.application.service.AssetCompensateService;
import com.smartmoldguard.device.domain.model.AssetCompensate;
import com.smartmoldguard.device.domain.model.WorkOrder;
import com.smartmoldguard.device.mapper.AssetCompensateMapper;
import com.smartmoldguard.device.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetCompensateServiceImpl extends ServiceImpl<AssetCompensateMapper, AssetCompensate> implements AssetCompensateService {

    private final WorkOrderMapper workOrderMapper;

    @Override
    public BigDecimal calculateCompensation(Long deviceId) {
        // 简单模拟计算逻辑：固定金额 50.00
        return new BigDecimal("50.00");
    }

    @Override
    @Transactional
    public void confirmCompensation(Long compensationId) {
        AssetCompensate compensation = getById(compensationId);
        if (compensation == null) {
            throw new RuntimeException("Compensation record not found");
        }

        compensation.setStatus("paid");
        updateById(compensation);

        // 关闭关联工单
        WorkOrder workOrder = workOrderMapper.selectById(compensation.getWorkOrderId());
        if (workOrder != null) {
            workOrder.setStatus("closed");
            workOrder.setUpdatedAt(LocalDateTime.now());
            workOrderMapper.updateById(workOrder);
        }
    }
}
