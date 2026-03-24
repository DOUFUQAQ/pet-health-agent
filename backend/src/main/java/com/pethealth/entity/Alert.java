package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警记录实体类
 */
@Data
@TableName("alert")
public class Alert implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 预警类型：temperature-体温异常，heart_rate-心率异常，activity-活动异常，behavior-行为异常，environment-环境异常
     */
    @TableField("alert_type")
    private String alertType;

    /**
     * 预警等级：0-信息，1-警告，2-错误，3-严重
     */
    @TableField("alert_level")
    private Integer alertLevel;

    /**
     * 预警标题
     */
    @TableField("alert_title")
    private String alertTitle;

    /**
     * 预警内容
     */
    @TableField("alert_content")
    private String alertContent;

    /**
     * 预警数据（JSON）
     */
    @TableField("alert_data")
    private String alertData;

    /**
     * 处置建议
     */
    @TableField("suggestion")
    private String suggestion;

    /**
     * 状态：0-未读，1-已读，2-已处理
     */
    @TableField("status")
    private Integer status;

    /**
     * 预警时间
     */
    @TableField("alert_time")
    private LocalDateTime alertTime;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
