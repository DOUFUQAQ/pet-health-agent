package com.pethealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.entity.DeviceData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备数据服务接口
 */
public interface IDeviceDataService extends IService<DeviceData> {

    /**
     * 保存设备数据（从MQTT接收）
     */
    void saveDeviceData(String deviceCode, String dataType, String dataValue);

    /**
     * 查询宠物某时间段某类型数据
     */
    List<DeviceData> queryByPetAndType(Long petId, String dataType,
                                       LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取宠物最新数据
     */
    DeviceData getLatest(Long petId, String dataType);

    /**
     * 获取近N天趋势数据
     */
    List<Map<String, Object>> getDailyTrend(Long petId, String dataType, Integer days);
}
