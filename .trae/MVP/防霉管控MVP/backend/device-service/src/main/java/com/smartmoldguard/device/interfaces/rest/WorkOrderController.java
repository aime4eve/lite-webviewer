package com.smartmoldguard.device.interfaces.rest;

import com.smartmoldguard.device.application.service.WorkOrderService;
import com.smartmoldguard.device.domain.model.WorkOrder;
import com.smartmoldguard.device.interfaces.rest.dto.BatchAssignRequest;
import com.smartmoldguard.device.interfaces.rest.dto.WorkOrderCreateRequest;
import com.smartmoldguard.device.interfaces.rest.dto.WorkOrderForwardRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单管理接口
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Order", description = "工单管理接口")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /**
     * 创建工单
     * @param request 创建请求
     * @return 工单实体
     */
    @PostMapping
    @Operation(summary = "创建工单")
    public ResponseEntity<WorkOrder> createWorkOrder(@RequestBody WorkOrderCreateRequest request) {
        return ResponseEntity.ok(workOrderService.createWorkOrder(request));
    }

    /**
     * 指派工单
     * @param id 工单ID
     * @param assignee 指派给谁
     * @return 工单实体
     */
    @PostMapping("/{id}/assign")
    @Operation(summary = "指派工单")
    public ResponseEntity<WorkOrder> assignWorkOrder(@PathVariable Long id, @RequestParam String assignee) {
        return ResponseEntity.ok(workOrderService.assignWorkOrder(id, assignee));
    }

    /**
     * 转发工单
     * @param id 工单ID
     * @param request 转发请求
     * @return 工单实体
     */
    @PostMapping("/{id}/forward")
    @Operation(summary = "转发工单")
    public ResponseEntity<WorkOrder> forwardWorkOrder(
            @PathVariable Long id, 
            @RequestBody WorkOrderForwardRequest request) {
        return ResponseEntity.ok(workOrderService.forwardWorkOrder(id, request.getToUser(), request.getReason()));
    }

    /**
     * 批量指派工单
     * @param request 批量指派请求
     * @return Void
     */
    @PostMapping("/batch-assign")
    @Operation(summary = "批量指派工单")
    public ResponseEntity<Void> batchAssign(@RequestBody BatchAssignRequest request) {
        workOrderService.batchAssignWorkOrders(request.getWorkOrderIds(), request.getAssignee());
        return ResponseEntity.ok().build();
    }

    /**
     * 查询我的工单
     * @param assignee 指派人ID
     * @return 工单列表
     */
    @GetMapping("/assigned")
    @Operation(summary = "查询我的工单")
    public ResponseEntity<List<WorkOrder>> getMyWorkOrders(@RequestParam String assignee) {
        return ResponseEntity.ok(workOrderService.getWorkOrdersByAssignee(assignee));
    }
}
