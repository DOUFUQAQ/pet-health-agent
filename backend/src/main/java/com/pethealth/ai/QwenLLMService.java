package com.pethealth.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.pethealth.common.ResultCode;
import com.pethealth.exception.BusinessException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通义千问文本大模型服务
 * 文档: https://help.aliyun.com/zh/dashscope/developer-reference/api-details
 */
@Slf4j
@Service
public class QwenLLMService {

    @Value("${ai.qwen.llm.api-key:${ai.qwen.api-key:}}")
    private String apiKey;

    @Value("${ai.qwen.llm.model:${ai.qwen.text-model:qwen-max}}")
    private String model;

    @Value("${ai.qwen.llm.api-url:${ai.qwen.text-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 单轮对话
     */
    public ChatResponse chat(String systemPrompt, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);

        return chatWithMessages(messages);
    }

    /**
     * 多轮对话
     */
    public ChatResponse chatWithMessages(List<Map<String, String>> messages) {
        long startTime = System.currentTimeMillis();

        JSONObject body = new JSONObject();
        body.put("model", model);

        JSONObject input = new JSONObject();
        input.put("messages", messages);
        body.put("input", input);

        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        parameters.put("temperature", 0.7);
        parameters.put("max_tokens", 2000);
        body.put("parameters", parameters);

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errMsg = response.body() != null ? response.body().string() : "";
                log.error("AI API调用失败: code={}, body={}", response.code(), errMsg);
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI服务调用失败: " + response.code());
            }

            String respStr = response.body().string();
            log.debug("AI响应: {}", respStr);

            JSONObject respJson = JSON.parseObject(respStr);
            JSONObject output = respJson.getJSONObject("output");

            ChatResponse result = new ChatResponse();
            result.setContent(output.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content"));
            result.setModel(model);

            JSONObject usage = respJson.getJSONObject("usage");
            if (usage != null) {
                result.setTotalTokens(usage.getIntValue("total_tokens"));
            }

            result.setResponseTime(System.currentTimeMillis() - startTime);
            return result;

        } catch (IOException e) {
            log.error("AI调用异常", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI调用异常: " + e.getMessage());
        }
    }

    /**
     * AI响应封装
     */
    @Data
    public static class ChatResponse {
        private String content;
        private String model;
        private Integer totalTokens;
        private Long responseTime;
    }
}
