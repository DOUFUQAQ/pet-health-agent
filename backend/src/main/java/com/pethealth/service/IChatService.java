package com.pethealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.dto.ChatRequestDTO;
import com.pethealth.entity.ChatHistory;

import java.util.List;

/**
 * AI对话服务接口
 */
public interface IChatService extends IService<ChatHistory> {

    /**
     * 发送消息并获取AI回复
     */
    ChatHistory chat(Long userId, ChatRequestDTO request);

    /**
     * 获取会话历史
     */
    List<ChatHistory> getSessionHistory(Long userId, String sessionId);

    /**
     * 获取用户的所有会话列表
     */
    List<String> listUserSessions(Long userId);

    /**
     * 删除会话
     */
    void deleteSession(Long userId, String sessionId);

    /**
     * 视频行为分析（结果存入 chat_history）
     */
    ChatHistory analyzeVideo(Long userId, Long petId, String videoUrl, String sessionId);
}
