package com.smartmoldguard.device.interfaces.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchAssignRequest {
    private List<Long> workOrderIds;
    private String assignee;
}
