package com.pethealth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * AI对话请求DTO
 */
@Data
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 宠物ID（可选）
     */
    private Long petId;

    /**
     * 消息类型：text-文本，image-图片，video-视频
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 媒体文件URL（图片或视频）
     */
    private String mediaUrl;
}
