package com.pethealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.ai.PromptBuilder;
import com.pethealth.ai.QwenLLMService;
import com.pethealth.ai.QwenVisionService;
import com.pethealth.common.Constants;
import com.pethealth.dto.ChatRequestDTO;
import com.pethealth.entity.ChatHistory;
import com.pethealth.entity.Pet;
import com.pethealth.mapper.ChatHistoryMapper;
import com.pethealth.mapper.PetMapper;
import com.pethealth.service.IChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI对话服务实现
 */
@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements IChatService {

    @Resource
    private PetMapper petMapper;

    @Resource
    private QwenLLMService llmService;

    @Resource
    private QwenVisionService visionService;

    @Override
    public ChatHistory chat(Long userId, ChatRequestDTO request) {
        Pet pet = null;
        if (request.getPetId() != null) {
            pet = petMapper.selectById(request.getPetId());
        }

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        String messageType = request.getMessageType() != null ? request.getMessageType() : Constants.MessageType.TEXT;
        QwenLLMService.ChatResponse aiResp;

        if (Constants.MessageType.IMAGE.equals(messageType) || Constants.MessageType.VIDEO.equals(messageType)) {
            // 多模态对话
            String prompt = request.getMessage();
            if (prompt == null || prompt.isEmpty()) {
                prompt = PromptBuilder.buildVideoAnalysisPrompt(
                        pet != null ? pet.getName() + "(" + pet.getSpecies() + ")" : "");
            }
            aiResp = visionService.analyze(request.getMediaUrl(), prompt,
                    Constants.MessageType.VIDEO.equals(messageType));
        } else {
            // 文本对话：构建多轮上下文
            List<Map<String, String>> messages = new ArrayList<>();
            String systemPrompt = PromptBuilder.buildSystemPrompt(pet);
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);

            // 加载历史对话（最近10条）
            LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatHistory::getSessionId, sessionId)
                   .orderByDesc(ChatHistory::getCreateTime)
                   .last("LIMIT 10");
            List<ChatHistory> history = baseMapper.selectList(wrapper);
            Collections.reverse(history);
            for (ChatHistory h : history) {
                Map<String, String> u = new HashMap<>();
                u.put("role", "user");
                u.put("content", h.getUserMessage());
                messages.add(u);
                Map<String, String> a = new HashMap<>();
                a.put("role", "assistant");
                a.put("content", h.getAiResponse());
                messages.add(a);
            }

            // 当前用户消息
            Map<String, String> u = new HashMap<>();
            u.put("role", "user");
            u.put("content", request.getMessage());
            messages.add(u);

            aiResp = llmService.chatWithMessages(messages);
        }

        // 保存对话记录
        ChatHistory chat = new ChatHistory();
        chat.setUserId(userId);
        chat.setPetId(request.getPetId());
        chat.setSessionId(sessionId);
        chat.setMessageType(messageType);
        chat.setUserMessage(request.getMessage());
        chat.setMediaUrl(request.getMediaUrl());
        chat.setAiResponse(aiResp.getContent());
        chat.setModelUsed(aiResp.getModel());
        chat.setTokensUsed(aiResp.getTotalTokens());
        chat.setResponseTime(aiResp.getResponseTime() != null ? aiResp.getResponseTime().intValue() : null);
        chat.setCreateTime(LocalDateTime.now());
        baseMapper.insert(chat);
        return chat;
    }

    @Override
    public List<ChatHistory> getSessionHistory(Long userId, String sessionId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId)
               .eq(ChatHistory::getSessionId, sessionId)
               .orderByAsc(ChatHistory::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<String> listUserSessions(Long userId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId)
               .select(ChatHistory::getSessionId)
               .groupBy(ChatHistory::getSessionId);
        return baseMapper.selectList(wrapper).stream()
                .map(ChatHistory::getSessionId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(Long userId, String sessionId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId)
               .eq(ChatHistory::getSessionId, sessionId);
        baseMapper.delete(wrapper);
    }

    @Override
    public ChatHistory analyzeVideo(Long userId, Long petId, String videoUrl, String sessionId) {
        Pet pet = petId != null ? petMapper.selectById(petId) : null;
        String petInfo = pet != null ? pet.getName() + "(" + pet.getSpecies() + ")" : "";
        String prompt = PromptBuilder.buildVideoAnalysisPrompt(petInfo);

        QwenLLMService.ChatResponse resp = visionService.analyze(videoUrl, prompt, true);

        String sid = (sessionId != null && !sessionId.isEmpty())
                ? sessionId
                : UUID.randomUUID().toString().replace("-", "");

        ChatHistory record = new ChatHistory();
        record.setUserId(userId);
        record.setPetId(petId);
        record.setSessionId(sid);
        record.setMessageType(Constants.MessageType.VIDEO);
        record.setUserMessage("[视频行为分析]");
        record.setMediaUrl(videoUrl);
        record.setAiResponse(resp.getContent());
        record.setModelUsed(resp.getModel());
        record.setTokensUsed(resp.getTotalTokens());
        record.setResponseTime(resp.getResponseTime() != null ? resp.getResponseTime().intValue() : null);
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);
        return record;
    }
}
