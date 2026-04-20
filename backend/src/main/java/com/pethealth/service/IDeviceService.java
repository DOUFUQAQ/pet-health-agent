package com.pethealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.dto.DeviceBindDTO;
import com.pethealth.entity.Device;

import java.util.List;

/**
 * 设备服务接口
 */
public interface IDeviceService extends IService<Device> {

    /**
     * 绑定设备
     */
    Device bindDevice(Long userId, DeviceBindDTO deviceBindDTO);

    /**
     * 解绑设备
     */
    void unbindDevice(Long userId, Long deviceId);

    /**
     * 获取用户所有设备
     */
    List<Device> listUserDevices(Long userId);

    /**
     * 获取宠物的所有设备
     */
    List<Device> listPetDevices(Long petId);

    /**
     * 更新设备状态（MQTT回调）
     */
    void updateDeviceStatus(String deviceCode, Integer status, Integer battery);

    /**
     * 发送设备控制指令
     */
    void sendControlCommand(Long userId, Long deviceId, String command, Object params);
}
