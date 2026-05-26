package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.common.ResultCode;
import com.pethealth.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传接口（图片 / 视频），返回可供 Qwen-VL 访问的 HTTP URL
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${upload.base-dir:uploads}")
    private String baseDir;

    @Value("${upload.base-url:}")
    private String configuredBaseUrl;

    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024L;  // 20 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024L; // 100 MB

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        validateFile(file, MAX_IMAGE_SIZE, new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"});
        String url = saveFile(file, "images", request);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    @PostMapping("/video")
    public Result<Map<String, String>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        validateFile(file, MAX_VIDEO_SIZE, new String[]{"video/mp4", "video/quicktime", "video/x-msvideo", "video/mpeg"});
        String url = saveFile(file, "videos", request);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    // ----------------------------------------------------------------

    private void validateFile(MultipartFile file, long maxSize, String[] allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED,
                    "文件大小超过限制（最大 " + maxSize / 1024 / 1024 + " MB）");
        }
        String contentType = file.getContentType();
        boolean typeOk = false;
        for (String t : allowedTypes) {
            if (t.equalsIgnoreCase(contentType)) {
                typeOk = true;
                break;
            }
        }
        if (!typeOk) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED,
                    "不支持的文件类型: " + contentType);
        }
    }

    private String saveFile(MultipartFile file, String subDir, HttpServletRequest request) {
        // 按日期分目录，避免单目录文件过多
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ext = getExtension(file.getOriginalFilename(), file.getContentType());
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 始终相对于项目运行目录解析，保证跨电脑移植
        java.nio.file.Path workDir = java.nio.file.Paths.get(System.getProperty("user.dir"));
        File baseDirFile = workDir.resolve(baseDir).toAbsolutePath().normalize().toFile();
        File destDir = new File(baseDirFile, subDir + File.separator + dateDir);
        if (!destDir.exists() && !destDir.mkdirs()) {
            log.error("无法创建上传目录: {}", destDir.getAbsolutePath());
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "无法创建上传目录: " + destDir.getAbsolutePath());
        }
        File dest = new File(destDir, filename);

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件保存失败: {}", dest.getAbsolutePath(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件保存失败: " + e.getMessage());
        }

        // 构造可访问 URL
        String baseUrl = configuredBaseUrl;
        if (baseUrl == null || baseUrl.isBlank()) {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String contextPath = request.getContextPath();
            baseUrl = scheme + "://" + host + (port == 80 || port == 443 ? "" : ":" + port) + contextPath;
        }
        // URL 路径使用正斜杠
        String urlPath = "/" + subDir + "/" + dateDir + "/" + filename;
        return baseUrl + "/files" + urlPath;
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        if (contentType != null) {
            switch (contentType.toLowerCase()) {
                case "image/jpeg": return ".jpg";
                case "image/png":  return ".png";
                case "image/gif":  return ".gif";
                case "image/webp": return ".webp";
                case "video/mp4":  return ".mp4";
                case "video/quicktime": return ".mov";
                default: return "";
            }
        }
        return "";
    }
}
