package com.smartmoldguard.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmoldguard.device.domain.model.WorkOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {
}
