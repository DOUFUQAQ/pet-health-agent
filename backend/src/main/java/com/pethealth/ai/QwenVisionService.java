package com.pethealth.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.pethealth.common.ResultCode;
import com.pethealth.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 通义千问多模态大模型服务（图片/视频理解）
 */
@Slf4j
@Service
public class QwenVisionService {

    @Value("${ai.qwen.vision.api-key:${ai.qwen.api-key:}}")
    private String apiKey;

    @Value("${ai.qwen.vision.model:${ai.qwen.vision-model:qwen-vl-max}}")
    private String model;

    @Value("${ai.qwen.vision.api-url:${ai.qwen.vision-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation}}")
    private String apiUrl;

    @Value("${upload.base-dir:uploads}")
    private String uploadBaseDir;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * 图片/视频分析
     *
     * @param mediaUrl 媒体URL（支持http/https链接，本地文件自动转base64）
     * @param prompt   分析提示词
     * @param isVideo  是否视频（否则为图片）
     */
    public QwenLLMService.ChatResponse analyze(String mediaUrl, String prompt, boolean isVideo) {
        // 本地文件转为 base64 data URL，DashScope 无法访问内网地址
        String resolvedUrl = isLocalUrl(mediaUrl) ? toBase64DataUrl(mediaUrl, isVideo) : mediaUrl;
        log.info("多模态分析: isVideo={}, originalUrl={}, resolvedUrl_prefix={}",
                isVideo, mediaUrl, resolvedUrl.substring(0, Math.min(80, resolvedUrl.length())));

        return doAnalyze(resolvedUrl, prompt, isVideo);
    }

    /**
     * 判断是否本地/内网 URL（DashScope 无法访问）
     */
    private boolean isLocalUrl(String url) {
        if (url == null) return false;
        String host = URI.create(url).getHost();
        if (host == null) return true;
        return host.equals("localhost") || host.equals("127.0.0.1")
                || host.startsWith("10.") || host.startsWith("172.16.")
                || host.startsWith("172.17.") || host.startsWith("172.18.")
                || host.startsWith("172.19.") || host.startsWith("172.20.")
                || host.startsWith("172.21.") || host.startsWith("172.22.")
                || host.startsWith("172.23.") || host.startsWith("172.24.")
                || host.startsWith("172.25.") || host.startsWith("172.26.")
                || host.startsWith("172.27.") || host.startsWith("172.28.")
                || host.startsWith("172.29.") || host.startsWith("172.30.")
                || host.startsWith("172.31.")
                || host.startsWith("192.168.") || host.startsWith("100.");
    }

    /**
     * 将本地文件的 HTTP URL 转为 base64 data URL
     */
    private String toBase64DataUrl(String url, boolean isVideo) {
        try {
            // URL 格式: http://host:port/api/files/images/20260526/xxx.png
            String path = URI.create(url).getPath();
            // 去掉 /api/files/ 前缀，得到 images/20260526/xxx.png
            String relativePath = path.replaceFirst("^/?(api/)?files/", "");

            Path workDir = Paths.get(System.getProperty("user.dir"));
            Path filePath = workDir.resolve(uploadBaseDir).resolve(relativePath).toAbsolutePath().normalize();

            log.info("本地文件转base64: url={}, filePath={}", url, filePath);

            byte[] bytes = Files.readAllBytes(filePath);
            String b64 = Base64.getEncoder().encodeToString(bytes);

            String mime = isVideo ? "video/mp4" : guessMime(relativePath);
            return "data:" + mime + ";base64," + b64;
        } catch (Exception e) {
            log.error("文件转base64失败: url={}", url, e);
            // 兜底：仍用原始 URL 尝试
            return url;
        }
    }

    private String guessMime(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mov")) return "video/quicktime";
        return "image/jpeg";
    }

    private QwenLLMService.ChatResponse doAnalyze(String mediaUrl, String prompt, boolean isVideo) {
        long startTime = System.currentTimeMillis();

        JSONObject body = new JSONObject();
        body.put("model", model);

        // 构造多模态消息
        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");

        JSONArray contentArr = new JSONArray();

        JSONObject mediaObj = new JSONObject();
        if (isVideo) {
            mediaObj.put("video", mediaUrl);
        } else {
            mediaObj.put("image", mediaUrl);
        }
        contentArr.add(mediaObj);

        JSONObject textObj = new JSONObject();
        textObj.put("text", prompt);
        contentArr.add(textObj);

        userMsg.put("content", contentArr);
        messages.add(userMsg);

        JSONObject input = new JSONObject();
        input.put("messages", messages);
        body.put("input", input);

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errMsg = response.body() != null ? response.body().string() : "";
                log.error("多模态AI调用失败: code={}, body={}", response.code(), errMsg);
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "多模态AI调用失败: " + response.code());
            }

            String respStr = response.body().string();
            log.debug("多模态AI响应: {}", respStr);

            JSONObject respJson = JSON.parseObject(respStr);
            JSONObject output = respJson.getJSONObject("output");

            QwenLLMService.ChatResponse result = new QwenLLMService.ChatResponse();

            // 解析content（可能为数组）
            Object content = output.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .get("content");

            String contentStr;
            if (content instanceof JSONArray) {
                JSONArray arr = (JSONArray) content;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    if (item.containsKey("text")) {
                        sb.append(item.getString("text"));
                    }
                }
                contentStr = sb.toString();
            } else {
                contentStr = content.toString();
            }

            result.setContent(contentStr);
            result.setModel(model);

            JSONObject usage = respJson.getJSONObject("usage");
            if (usage != null) {
                result.setTotalTokens(usage.getIntValue("total_tokens"));
            }

            result.setResponseTime(System.currentTimeMillis() - startTime);
            return result;

        } catch (IOException e) {
            log.error("多模态AI调用异常", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "多模态AI调用异常: " + e.getMessage());
        }
    }
}
