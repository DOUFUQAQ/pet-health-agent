package com.pethealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.entity.HealthRecord;
import com.pethealth.vo.HealthDashboardVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 健康服务接口
 */
public interface IHealthService extends IService<HealthRecord> {

    /**
     * 获取健康看板数据
     */
    HealthDashboardVO getDashboard(Long userId, Long petId);

    /**
     * 生成每日健康报告（定时任务调用）
     */
    HealthRecord generateDailyReport(Long petId, LocalDate date);

    /**
     * 获取健康历史记录
     */
    List<HealthRecord> getHistoryRecords(Long petId, Integer days);

    /**
     * 计算健康评分
     */
    Integer calculateHealthScore(Long petId, LocalDate date);

    /**
     * 触发AI健康分析
     */
    String analyzeHealthWithAI(Long petId, LocalDate date);
}
