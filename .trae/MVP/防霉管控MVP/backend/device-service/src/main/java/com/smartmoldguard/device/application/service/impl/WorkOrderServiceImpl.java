package com.smartmoldguard.device.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmoldguard.device.application.service.WorkOrderService;
import com.smartmoldguard.device.domain.model.WorkOrder;
import com.smartmoldguard.device.interfaces.rest.dto.WorkOrderCreateRequest;
import com.smartmoldguard.device.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单服务实现
 */
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    @Override
    @Transactional
    public WorkOrder createWorkOrder(WorkOrderCreateRequest request) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setDeviceId(request.getDeviceId());
        workOrder.setType(request.getType());
        workOrder.setDescription(request.getDescription());
        workOrder.setStatus("pending");
        if (request.getAssignee() != null) {
            workOrder.setAssignee(request.getAssignee());
            workOrder.setStatus("assigned");
        }
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());
        
        save(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public WorkOrder assignWorkOrder(Long workOrderId, String assignee) {
        WorkOrder workOrder = getById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("WorkOrder not found");
        }
        workOrder.setAssignee(assignee);
        workOrder.setStatus("assigned");
        workOrder.setUpdatedAt(LocalDateTime.now());
        updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public WorkOrder forwardWorkOrder(Long workOrderId, String toUser, String reason) {
        WorkOrder workOrder = getById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("WorkOrder not found");
        }
        String oldAssignee = workOrder.getAssignee();
        workOrder.setAssignee(toUser);
        workOrder.setStatus("assigned");
        
        // 追加流转记录到描述中 (实际生产建议使用独立的日志表)
        String forwardLog = String.format("\n[Forward] From %s to %s: %s", oldAssignee, toUser, reason);
        if (workOrder.getDescription() != null) {
            workOrder.setDescription(workOrder.getDescription() + forwardLog);
        } else {
            workOrder.setDescription(forwardLog);
        }
        
        workOrder.setUpdatedAt(LocalDateTime.now());
        updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public void batchAssignWorkOrders(List<Long> workOrderIds, String assignee) {
        List<WorkOrder> orders = listByIds(workOrderIds);
        orders.forEach(o -> {
            o.setAssignee(assignee);
            o.setStatus("assigned");
            o.setUpdatedAt(LocalDateTime.now());
        });
        updateBatchById(orders);
    }

    @Override
    public List<WorkOrder> getWorkOrdersByAssignee(String assignee) {
        return list(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getAssignee, assignee)
                .orderByDesc(WorkOrder::getCreatedAt));
    }
}
