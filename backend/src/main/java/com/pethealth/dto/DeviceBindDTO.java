package com.pethealth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 设备绑定DTO
 */
@Data
public class DeviceBindDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备唯一标识
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    /**
     * 绑定宠物ID
     */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
}
