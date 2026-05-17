package com.pethealth.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 健康看板VO
 */
@Data
public class HealthDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 今日健康评分
     */
    private Integer healthScore;

    /**
     * 实时体温（°C）
     */
    private BigDecimal currentTemperature;

    /**
     * 实时心率（次/分）
     */
    private Integer currentHeartRate;

    /**
     * 实时环境温度（°C，DHT22）
     */
    private BigDecimal currentEnvTemp;

    /**
     * 实时环境湿度（%RH，DHT22）
     */
    private BigDecimal currentHumidity;

    /**
     * 今日活动步数
     */
    private Integer todaySteps;

    /**
     * 今日睡眠时长（分钟）
     */
    private Integer todaySleep;

    /**
     * 今日进食量（克）
     */
    private Integer todayFood;

    /**
     * 今日饮水量（毫升）
     */
    private Integer todayWater;

    /**
     * 未读预警数量
     */
    private Integer unreadAlerts;

    /**
     * 近7天体温趋势
     */
    private List<TrendData> temperatureTrend;

    /**
     * 近7天心率趋势
     */
    private List<TrendData> heartRateTrend;

    /**
     * 近7天活动量趋势
     */
    private List<TrendData> activityTrend;

    /**
     * AI健康分析
     */
    private String aiAnalysis;

    /**
     * 趋势数据内部类
     */
    @Data
    public static class TrendData implements Serializable {
        private String date;
        private BigDecimal value;
    }
}
