package com.smartmoldguard.device.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmoldguard.device.domain.model.Alarm;
import com.smartmoldguard.device.interfaces.rest.dto.AlarmStatisticsDto;

import java.util.List;

/**
 * 告警服务接口
 */
public interface AlarmService extends IService<Alarm> {
    
    /**
     * 查询告警
     * @param deviceId 设备ID (可选)
     * @param status 状态 (可选)
     * @return 告警列表
     */
    List<Alarm> getAlarms(Long deviceId, String status);

    /**
     * 确认告警
     * @param id 告警ID
     */
    void confirmAlarm(Long id);

    /**
     * 清除告警
     * @param id 告警ID
     */
    void clearAlarm(Long id);

    /**
     * 获取告警统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    AlarmStatisticsDto getStatistics(String startDate, String endDate);
}
