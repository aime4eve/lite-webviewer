package com.smartmoldguard.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmoldguard.device.domain.model.Alarm;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警 Mapper 接口
 */
@Mapper
public interface AlarmMapper extends BaseMapper<Alarm> {
}
