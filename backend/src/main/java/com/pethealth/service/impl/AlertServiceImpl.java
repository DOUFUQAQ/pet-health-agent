package com.pethealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.common.Constants;
import com.pethealth.entity.Alert;
import com.pethealth.entity.Device;
import com.pethealth.entity.Pet;
import com.pethealth.mapper.AlertMapper;
import com.pethealth.mapper.DeviceMapper;
import com.pethealth.mapper.PetMapper;
import com.pethealth.service.IAlertService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警服务实现
 */
@Slf4j
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements IAlertService {

    @Resource
    private PetMapper petMapper;

    @Resource
    private DeviceMapper deviceMapper;

    // 阈值配置（从application.yml读取）
    @Value("${health.threshold.temperature.min:37.5}")
    private Double tempMin;

    @Value("${health.threshold.temperature.max:39.5}")
    private Double tempMax;

    @Value("${health.threshold.heart-rate.dog.min:60}")
    private Double dogHrMin;

    @Value("${health.threshold.heart-rate.dog.max:160}")
    private Double dogHrMax;

    @Value("${health.threshold.heart-rate.cat.min:120}")
    private Double catHrMin;

    @Value("${health.threshold.heart-rate.cat.max:220}")
    private Double catHrMax;

    @Override
    public IPage<Alert> listUserAlerts(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getUserId, userId)
               .eq(status != null, Alert::getStatus, status)
               .orderByDesc(Alert::getAlertTime);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Alert createAlert(Long petId, Long userId, String alertType, Integer level,
                             String title, String content, String data, String suggestion) {
        Alert alert = new Alert();
        alert.setPetId(petId);
        alert.setUserId(userId);
        alert.setAlertType(alertType);
        alert.setAlertLevel(level);
        alert.setAlertTitle(title);
        alert.setAlertContent(content);
        alert.setAlertData(data);
        alert.setSuggestion(suggestion);
        alert.setStatus(Constants.AlertStatus.UNREAD);
        alert.setAlertTime(LocalDateTime.now());
        alert.setCreateTime(LocalDateTime.now());
        baseMapper.insert(alert);
        log.info("创建预警: petId={}, type={}, level={}", petId, alertType, level);
        return alert;
    }

    @Override
    public void markAsRead(Long userId, Long alertId) {
        Alert alert = baseMapper.selectById(alertId);
        if (alert == null || !alert.getUserId().equals(userId)) return;
        alert.setStatus(Constants.AlertStatus.READ);
        alert.setReadTime(LocalDateTime.now());
        baseMapper.updateById(alert);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getUserId, userId)
               .eq(Alert::getStatus, Constants.AlertStatus.UNREAD);
        List<Alert> alerts = baseMapper.selectList(wrapper);
        for (Alert alert : alerts) {
            alert.setStatus(Constants.AlertStatus.READ);
            alert.setReadTime(LocalDateTime.now());
        }
        this.updateBatchById(alerts);
    }

    @Override
    public void handleAlert(Long userId, Long alertId) {
        Alert alert = baseMapper.selectById(alertId);
        if (alert == null || !alert.getUserId().equals(userId)) return;
        alert.setStatus(Constants.AlertStatus.HANDLED);
        alert.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(alert);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getUserId, userId)
               .eq(Alert::getStatus, Constants.AlertStatus.UNREAD);
        return Math.toIntExact(baseMapper.selectCount(wrapper));
    }

    @Override
    public void checkThresholdAndAlert(Long petId, String dataType, Double value) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) return;

        String alertType = null;
        Integer level = null;
        String title = null;
        String content = null;
        String suggestion = null;

        switch (dataType) {
            case Constants.DataType.TEMPERATURE:
                // 低于 35°C 视为传感器未贴宠物（如 MLX90614 测到环境/物体温度），不触发预警
                if (value < 35.0) {
                    return;
                }
                if (value < tempMin || value > tempMax) {
                    alertType = Constants.AlertType.TEMPERATURE;
                    level = (value < tempMin - 1 || value > tempMax + 1)
                            ? Constants.AlertLevel.CRITICAL : Constants.AlertLevel.WARNING;
                    title = "体温异常预警";
                    content = String.format("%s的体温为%.1f°C，超出正常范围", pet.getName(), value);
                    suggestion = "请尽快就医检查";
                }
                break;
            case Constants.DataType.HEART_RATE:
                boolean isDog = Constants.PetSpecies.DOG.equalsIgnoreCase(pet.getSpecies());
                double hrMin = isDog ? dogHrMin : catHrMin;
                double hrMax = isDog ? dogHrMax : catHrMax;
                if (value < hrMin || value > hrMax) {
                    alertType = Constants.AlertType.HEART_RATE;
                    level = Constants.AlertLevel.WARNING;
                    title = "心率异常预警";
                    content = String.format("%s的心率为%.0f次/分", pet.getName(), value);
                    suggestion = "请观察宠物状态，必要时就医";
                }
                break;
        }

        if (alertType != null) {
            String dataJson = String.format("{\"dataType\":\"%s\",\"value\":%s}", dataType, value);
            createAlert(petId, pet.getUserId(), alertType, level, title, content, dataJson, suggestion);
        }
    }

    // 不需要使用deviceMapper，保留占位避免lint警告
    @SuppressWarnings("unused")
    private Device _placeholder(Long id) {
        return deviceMapper.selectById(id);
    }
}
