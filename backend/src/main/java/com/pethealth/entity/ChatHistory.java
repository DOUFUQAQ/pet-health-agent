package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI对话历史实体类
 */
@Data
@TableName("chat_history")
public class ChatHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对话ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 消息类型：text-文本，image-图片，video-视频
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 用户消息
     */
    @TableField("user_message")
    private String userMessage;

    /**
     * 媒体文件URL
     */
    @TableField("media_url")
    private String mediaUrl;

    /**
     * AI回复
     */
    @TableField("ai_response")
    private String aiResponse;

    /**
     * 使用的模型
     */
    @TableField("model_used")
    private String modelUsed;

    /**
     * 消耗的Token数
     */
    @TableField("tokens_used")
    private Integer tokensUsed;

    /**
     * 响应时间（毫秒）
     */
    @TableField("response_time")
    private Integer responseTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
