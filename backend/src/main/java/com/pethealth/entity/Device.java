package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备实体类
 */
@Data
@TableName("device")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备唯一标识
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备类型：collar-项圈，camera-摄像头，sensor-传感器，feeder-喂食器
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 绑定用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 绑定宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 状态：0-离线，1-在线，2-故障
     */
    @TableField("status")
    private Integer status;

    /**
     * 电量（%）
     */
    @TableField("battery")
    private Integer battery;

    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 设备配置（JSON）
     */
    @TableField("config")
    private String config;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
