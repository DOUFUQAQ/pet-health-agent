package com.pethealth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.entity.Alert;

/**
 * 预警服务接口
 */
public interface IAlertService extends IService<Alert> {

    /**
     * 分页查询用户的预警列表
     */
    IPage<Alert> listUserAlerts(Long userId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 创建预警
     */
    Alert createAlert(Long petId, Long userId, String alertType, Integer level,
                      String title, String content, String data, String suggestion);

    /**
     * 标记为已读
     */
    void markAsRead(Long userId, Long alertId);

    /**
     * 批量标记已读
     */
    void markAllAsRead(Long userId);

    /**
     * 处理预警
     */
    void handleAlert(Long userId, Long alertId);

    /**
     * 获取未读预警数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 检查并触发阈值预警
     */
    void checkThresholdAndAlert(Long petId, String dataType, Double value);
}
