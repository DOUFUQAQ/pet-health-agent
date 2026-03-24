package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 养护日志实体类
 */
@Data
@TableName("care_log")
public class CareLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
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
     * 日志类型：feeding-喂食，medication-用药，vaccination-疫苗，checkup-体检，grooming-美容，other-其他
     */
    @TableField("log_type")
    private String logType;

    /**
     * 日志标题
     */
    @TableField("log_title")
    private String logTitle;

    /**
     * 日志内容
     */
    @TableField("log_content")
    private String logContent;

    /**
     * 日志数据（JSON）
     */
    @TableField("log_data")
    private String logData;

    /**
     * 图片URLs（JSON数组）
     */
    @TableField("images")
    private String images;

    /**
     * 日志时间
     */
    @TableField("log_time")
    private LocalDateTime logTime;

    /**
     * 提醒时间（如疫苗到期）
     */
    @TableField("reminder_time")
    private LocalDateTime reminderTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
