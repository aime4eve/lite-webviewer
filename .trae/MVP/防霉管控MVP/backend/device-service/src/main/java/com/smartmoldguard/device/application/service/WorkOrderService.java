package com.smartmoldguard.device.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmoldguard.device.domain.model.WorkOrder;
import com.smartmoldguard.device.interfaces.rest.dto.WorkOrderCreateRequest;

import java.util.List;

/**
 * 工单服务接口
 */
public interface WorkOrderService extends IService<WorkOrder> {
    
    /**
     * 创建工单
     * @param request 创建请求
     * @return 工单实体
     */
    WorkOrder createWorkOrder(WorkOrderCreateRequest request);

    /**
     * 指派工单
     * @param workOrderId 工单ID
     * @param assignee 指派给谁
     * @return 工单实体
     */
    WorkOrder assignWorkOrder(Long workOrderId, String assignee);

    /**
     * 转发工单
     * @param workOrderId 工单ID
     * @param toUser 转发给谁
     * @param reason 转发原因
     * @return 工单实体
     */
    WorkOrder forwardWorkOrder(Long workOrderId, String toUser, String reason);

    /**
     * 批量指派工单
     * @param workOrderIds 工单ID列表
     * @param assignee 指派给谁
     */
    void batchAssignWorkOrders(List<Long> workOrderIds, String assignee);

    /**
     * 查询我的工单
     * @param assignee 指派人ID
     * @return 工单列表
     */
    List<WorkOrder> getWorkOrdersByAssignee(String assignee);
}
