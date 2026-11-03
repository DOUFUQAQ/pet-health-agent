package com.pethealth.mqtt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pethealth.entity.Device;
import com.pethealth.mapper.DeviceMapper;
import com.pethealth.service.IDeviceDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 心率模拟器：MAX30102 模块当前在当前模块在演示阶段还不稳定，
 * 用 sin 波 + 随机噪声生成 70-90 bpm 范围内的心率数据，
 * 让前端"实时心率" KPI 卡片和 7 日趋势曲线有真实活动感。
 * 待硬件模块更换后可移除此类，由 firmware 上报真实值替代。
 */
@Slf4j
@Component
public class HeartRateSimulator {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private IDeviceDataService deviceDataService;

    private long tick = 0;

    @Scheduled(fixedRate = 60_000L, initialDelay = 15_000L)
    public void simulate() {
        // 仅给已绑定宠物的设备模拟（避免无主数据）
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Device::getPetId);
        List<Device> devices = deviceMapper.selectList(wrapper);
        if (devices.isEmpty()) {
            return;
        }

        // 70-90 bpm 之间，用 sin 波 + ±2 bpm 随机噪声，呈现自然呼吸性窦性变异
        double base = 80.0;
        double sineAmplitude = 8.0;
        double phase = (tick % 60) / 60.0 * 2 * Math.PI;
        double noise = (Math.random() - 0.5) * 4.0;
        int bpm = (int) Math.round(base + sineAmplitude * Math.sin(phase) + noise);
        tick++;

        String payload = String.format(
                "{\"dataType\":\"heart_rate\",\"value\":%d,\"unit\":\"bpm\",\"source\":\"sim\"}",
                bpm);

        for (Device device : devices) {
            deviceDataService.saveDeviceData(device.getDeviceId(), "heart_rate", payload);
        }
        log.debug("心率模拟器写入 {}bpm，设备数 {}", bpm, devices.size());
    }
}
