package com.smartmoldguard.device.application.service;

import com.smartmoldguard.device.application.service.impl.WorkOrderServiceImpl;
import com.smartmoldguard.device.domain.model.WorkOrder;
import com.smartmoldguard.device.interfaces.rest.dto.WorkOrderCreateRequest;
import com.smartmoldguard.device.mapper.WorkOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkOrderServiceTest {

    @Mock
    private WorkOrderMapper workOrderMapper;

    @InjectMocks
    private WorkOrderServiceImpl workOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Manually set baseMapper for MyBatis-Plus ServiceImpl
        try {
            java.lang.reflect.Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(workOrderService, workOrderMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateCleaningTask() {
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setDeviceId(100L);
        request.setType("cleaning");
        request.setDescription("Clean the filter");
        request.setAssignee("cleaner01");

        when(workOrderMapper.insert(any(WorkOrder.class))).thenReturn(1);

        WorkOrder result = workOrderService.createWorkOrder(request);

        assertNotNull(result);
        assertEquals("cleaning", result.getType());
        assertEquals("assigned", result.getStatus());
        assertEquals("cleaner01", result.getAssignee());
        
        verify(workOrderMapper).insert(any(WorkOrder.class));
    }

    @Test
    void testAssignTask() {
        WorkOrder existing = new WorkOrder();
        existing.setId(1L);
        existing.setStatus("pending");

        when(workOrderMapper.selectById(1L)).thenReturn(existing);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);

        WorkOrder result = workOrderService.assignWorkOrder(1L, "tech01");

        assertEquals("assigned", result.getStatus());
        assertEquals("tech01", result.getAssignee());
    }
}
