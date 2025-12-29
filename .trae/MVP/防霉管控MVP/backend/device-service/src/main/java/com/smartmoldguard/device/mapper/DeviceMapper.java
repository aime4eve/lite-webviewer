package com.smartmoldguard.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmoldguard.device.domain.model.Device;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}
