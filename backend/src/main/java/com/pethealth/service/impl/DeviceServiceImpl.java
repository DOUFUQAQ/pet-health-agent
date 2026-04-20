package com.pethealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.common.ResultCode;
import com.pethealth.dto.DeviceBindDTO;
import com.pethealth.entity.Device;
import com.pethealth.entity.Pet;
import com.pethealth.exception.BusinessException;
import com.pethealth.mapper.DeviceMapper;
import com.pethealth.mapper.PetMapper;
import com.pethealth.mqtt.MqttPublisher;
import com.pethealth.service.IDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备服务实现
 */
@Slf4j
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    @Resource
    private PetMapper petMapper;

    @Resource
    private MqttPublisher mqttPublisher;

    @Override
    public Device bindDevice(Long userId, DeviceBindDTO dto) {
        // 验证宠物归属
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }

        // 检查设备是否已被绑定
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getDeviceId, dto.getDeviceId());
        Device existing = baseMapper.selectOne(wrapper);

        Device device;
        if (existing != null) {
            if (existing.getUserId() != null && !existing.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.DEVICE_ALREADY_BOUND);
            }
            device = existing;
        } else {
            device = new Device();
            BeanUtils.copyProperties(dto, device);
            device.setCreateTime(LocalDateTime.now());
            device.setDeleted(0);
        }

        device.setUserId(userId);
        device.setPetId(dto.getPetId());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setStatus(0); // 默认离线
        device.setUpdateTime(LocalDateTime.now());

        if (existing != null) {
            baseMapper.updateById(device);
        } else {
            baseMapper.insert(device);
        }
        return device;
    }

    @Override
    public void unbindDevice(Long userId, Long deviceId) {
        Device device = baseMapper.selectById(deviceId);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND);
        }
        device.setUserId(null);
        device.setPetId(null);
        device.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(device);
    }

    @Override
    public List<Device> listUserDevices(Long userId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getUserId, userId)
               .orderByDesc(Device::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Device> listPetDevices(Long petId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getPetId, petId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void updateDeviceStatus(String deviceCode, Integer status, Integer battery) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getDeviceId, deviceCode);
        Device device = baseMapper.selectOne(wrapper);
        if (device == null) {
            log.warn("设备不存在: {}", deviceCode);
            return;
        }
        if (status != null) device.setStatus(status);
        if (battery != null) device.setBattery(battery);
        device.setLastOnlineTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(device);
    }

    @Override
    public void sendControlCommand(Long userId, Long deviceId, String command, Object params) {
        Device device = baseMapper.selectById(deviceId);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND);
        }
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("command", command);
        cmd.put("params", params);
        cmd.put("timestamp", System.currentTimeMillis());
        mqttPublisher.sendDeviceControl(device.getDeviceId(), cmd);
    }
}
