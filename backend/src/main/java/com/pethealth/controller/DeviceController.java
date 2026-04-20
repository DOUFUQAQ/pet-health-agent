package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.dto.DeviceBindDTO;
import com.pethealth.entity.Device;
import com.pethealth.service.IDeviceService;
import com.pethealth.utils.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Resource
    private IDeviceService deviceService;

    /**
     * 获取用户设备列表
     */
    @GetMapping("/list")
    public Result<List<Device>> list() {
        return Result.success(deviceService.listUserDevices(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 获取宠物的设备列表
     */
    @GetMapping("/pet/{petId}")
    public Result<List<Device>> listByPet(@PathVariable Long petId) {
        return Result.success(deviceService.listPetDevices(petId));
    }

    /**
     * 绑定设备
     */
    @PostMapping("/bind")
    public Result<Device> bind(@Valid @RequestBody DeviceBindDTO dto) {
        return Result.success(deviceService.bindDevice(SecurityUtil.getCurrentUserId(), dto));
    }

    /**
     * 解绑设备
     */
    @PostMapping("/unbind/{id}")
    public Result<Void> unbind(@PathVariable Long id) {
        deviceService.unbindDevice(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    /**
     * 发送设备控制指令
     */
    @PostMapping("/control/{id}")
    public Result<Void> control(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String command = (String) params.get("command");
        Object cmdParams = params.get("params");
        deviceService.sendControlCommand(SecurityUtil.getCurrentUserId(), id, command, cmdParams);
        return Result.success();
    }
}
