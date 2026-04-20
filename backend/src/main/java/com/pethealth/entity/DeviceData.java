package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备数据实体类（时序数据）
 */
@Data
@TableName("device_data")
public class DeviceData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 数据类型：temperature-体温，heart_rate-心率，activity-活动量，location-位置，environment-环境，video-视频
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 数据值（JSON格式）
     */
    @TableField("data_value")
    private String dataValue;

    /**
     * 数据时间戳
     */
    @TableField("timestamp")
    private LocalDateTime timestamp;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
