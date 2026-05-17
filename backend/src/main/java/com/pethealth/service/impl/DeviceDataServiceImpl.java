package com.pethealth.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.entity.Device;
import com.pethealth.entity.DeviceData;
import com.pethealth.entity.HealthRecord;
import com.pethealth.mapper.DeviceDataMapper;
import com.pethealth.mapper.DeviceMapper;
import com.pethealth.mapper.HealthRecordMapper;
import com.pethealth.service.IAlertService;
import com.pethealth.service.IDeviceDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备数据服务实现
 */
@Slf4j
@Service
public class DeviceDataServiceImpl extends ServiceImpl<DeviceDataMapper, DeviceData> implements IDeviceDataService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private IAlertService alertService;

    @Resource
    private HealthRecordMapper healthRecordMapper;

    @Override
    public void saveDeviceData(String deviceCode, String dataType, String dataValue) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getDeviceId, deviceCode);
        Device device = deviceMapper.selectOne(wrapper);
        if (device == null) {
            log.warn("未知设备上报数据: {}", deviceCode);
            return;
        }

        DeviceData data = new DeviceData();
        data.setDeviceId(device.getId());
        data.setPetId(device.getPetId());
        data.setDataType(dataType);
        data.setDataValue(dataValue);
        data.setTimestamp(LocalDateTime.now());
        data.setCreateTime(LocalDateTime.now());
        baseMapper.insert(data);

        // 解析数据值
        try {
            JSONObject json = JSON.parseObject(dataValue);
            Double value = json.getDouble("value");

            if (value != null && device.getPetId() != null) {
                // step 类型：增量累加到当日 health_record.activity_steps，不进阈值预警
                if ("step".equals(dataType)) {
                    accumulateSteps(device.getPetId(), value.intValue());
                } else {
                    alertService.checkThresholdAndAlert(device.getPetId(), dataType, value);
                }
            }
        } catch (Exception e) {
            log.debug("数据值解析失败，跳过后处理: {}", dataValue);
        }
    }

    /** 把步数增量累加到当日 health_record；记录不存在则插入 */
    private void accumulateSteps(Long petId, int delta) {
        if (delta <= 0) return;
        LocalDate today = LocalDate.now();
        HealthRecord record = healthRecordMapper.selectByPetAndDate(petId, today);
        if (record == null) {
            record = new HealthRecord();
            record.setPetId(petId);
            record.setRecordDate(today);
            record.setActivitySteps(delta);
            record.setHealthScore(85);
            record.setCreateTime(LocalDateTime.now());
            healthRecordMapper.insert(record);
        } else {
            int current = record.getActivitySteps() == null ? 0 : record.getActivitySteps();
            record.setActivitySteps(current + delta);
            healthRecordMapper.updateById(record);
        }
    }

    @Override
    public List<DeviceData> queryByPetAndType(Long petId, String dataType,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectByPetAndTypeAndTime(petId, dataType, startTime, endTime);
    }

    @Override
    public DeviceData getLatest(Long petId, String dataType) {
        return baseMapper.selectLatestByPetAndType(petId, dataType);
    }

    @Override
    public List<Map<String, Object>> getDailyTrend(Long petId, String dataType, Integer days) {
        return baseMapper.selectDailyAggregate(petId, dataType, days);
    }
}
