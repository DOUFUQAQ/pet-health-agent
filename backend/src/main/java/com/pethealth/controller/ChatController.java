package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.dto.ChatRequestDTO;
import com.pethealth.entity.ChatHistory;
import com.pethealth.service.IChatService;
import com.pethealth.utils.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI对话控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private IChatService chatService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<ChatHistory> send(@Valid @RequestBody ChatRequestDTO request) {
        return Result.success(chatService.chat(SecurityUtil.getCurrentUserId(), request));
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/session/{sessionId}")
    public Result<List<ChatHistory>> sessionHistory(@PathVariable String sessionId) {
        return Result.success(chatService.getSessionHistory(SecurityUtil.getCurrentUserId(), sessionId));
    }

    /**
     * 获取所有会话列表
     */
    @GetMapping("/sessions")
    public Result<List<String>> sessions() {
        return Result.success(chatService.listUserSessions(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        chatService.deleteSession(SecurityUtil.getCurrentUserId(), sessionId);
        return Result.success();
    }

    /**
     * 视频行为分析
     */
    @PostMapping("/analyze/video")
    public Result<ChatHistory> analyzeVideo(@RequestBody Map<String, Object> params) {
        Object petIdObj = params.get("petId");
        Long petId = petIdObj != null ? Long.valueOf(petIdObj.toString()) : null;
        String videoUrl = (String) params.get("videoUrl");
        String sessionId = params.containsKey("sessionId") ? (String) params.get("sessionId") : null;
        if (videoUrl == null || videoUrl.isBlank()) {
            return Result.error(com.pethealth.common.ResultCode.BAD_REQUEST);
        }
        return Result.success(chatService.analyzeVideo(SecurityUtil.getCurrentUserId(), petId, videoUrl, sessionId));
    }
}
