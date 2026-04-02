package com.pethealth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 养护日志DTO
 */
@Data
public class CareLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID（更新时需要）
     */
    private Long id;

    /**
     * 宠物ID
     */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    /**
     * 日志类型
     */
    @NotBlank(message = "日志类型不能为空")
    private String logType;

    /**
     * 日志标题
     */
    @NotBlank(message = "日志标题不能为空")
    private String logTitle;

    /**
     * 日志内容
     */
    private String logContent;

    /**
     * 日志数据（JSON）
     */
    private String logData;

    /**
     * 图片URLs（JSON数组）
     */
    private String images;

    /**
     * 日志时间
     */
    @NotNull(message = "日志时间不能为空")
    private LocalDateTime logTime;

    /**
     * 提醒时间
     */
    private LocalDateTime reminderTime;
}
