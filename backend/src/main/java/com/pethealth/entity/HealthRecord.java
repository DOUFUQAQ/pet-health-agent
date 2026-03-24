package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康记录实体类（每日汇总）
 */
@Data
@TableName("health_record")
public class HealthRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 记录日期
     */
    @TableField("record_date")
    private LocalDate recordDate;

    /**
     * 平均体温（°C）
     */
    @TableField("temperature_avg")
    private BigDecimal temperatureAvg;

    /**
     * 最低体温
     */
    @TableField("temperature_min")
    private BigDecimal temperatureMin;

    /**
     * 最高体温
     */
    @TableField("temperature_max")
    private BigDecimal temperatureMax;

    /**
     * 平均心率（次/分）
     */
    @TableField("heart_rate_avg")
    private Integer heartRateAvg;

    /**
     * 最低心率
     */
    @TableField("heart_rate_min")
    private Integer heartRateMin;

    /**
     * 最高心率
     */
    @TableField("heart_rate_max")
    private Integer heartRateMax;

    /**
     * 活动步数
     */
    @TableField("activity_steps")
    private Integer activitySteps;

    /**
     * 活动距离（米）
     */
    @TableField("activity_distance")
    private BigDecimal activityDistance;

    /**
     * 睡眠时长（分钟）
     */
    @TableField("sleep_duration")
    private Integer sleepDuration;

    /**
     * 进食量（克）
     */
    @TableField("food_intake")
    private Integer foodIntake;

    /**
     * 饮水量（毫升）
     */
    @TableField("water_intake")
    private Integer waterIntake;

    /**
     * 异常行为记录（JSON）
     */
    @TableField("abnormal_behaviors")
    private String abnormalBehaviors;

    /**
     * 健康评分（0-100）
     */
    @TableField("health_score")
    private Integer healthScore;

    /**
     * AI分析结果
     */
    @TableField("ai_analysis")
    private String aiAnalysis;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
