package com.pethealth.vo;

import lombok.Data;

/**
 * 管理端概览统计
 */
@Data
public class AdminOverviewVO {
    private Long totalUsers;
    private Long totalPets;
    private Long totalDevices;
    private Long onlineDevices;
    private Long unreadAlerts;
    private Long todayAlerts;
}
