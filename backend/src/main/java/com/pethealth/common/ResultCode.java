package com.pethealth.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    VALIDATION_ERROR(422, "数据验证失败"),

    // 服务端错误 5xx
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    PASSWORD_ERROR(1005, "密码错误"),
    OLD_PASSWORD_ERROR(1006, "原密码错误"),
    
    PET_NOT_FOUND(1101, "宠物不存在"),
    PET_NAME_REQUIRED(1102, "宠物名称不能为空"),
    
    DEVICE_NOT_FOUND(1201, "设备不存在"),
    DEVICE_ALREADY_BOUND(1202, "设备已被绑定"),
    DEVICE_OFFLINE(1203, "设备离线"),
    DEVICE_CONTROL_FAILED(1204, "设备控制失败"),
    
    HEALTH_DATA_NOT_FOUND(1301, "健康数据不存在"),
    HEALTH_RECORD_NOT_FOUND(1302, "健康记录不存在"),
    
    ALERT_NOT_FOUND(1401, "预警信息不存在"),
    
    // AI相关错误 2xxx
    AI_SERVICE_ERROR(2001, "AI服务调用失败"),
    AI_API_KEY_INVALID(2002, "AI API密钥无效"),
    AI_REQUEST_TIMEOUT(2003, "AI请求超时"),
    AI_QUOTA_EXCEEDED(2004, "AI调用次数已达上限"),
    
    // MQTT相关错误 3xxx
    MQTT_CONNECTION_ERROR(3001, "MQTT连接失败"),
    MQTT_PUBLISH_ERROR(3002, "MQTT消息发布失败"),
    MQTT_SUBSCRIBE_ERROR(3003, "MQTT主题订阅失败"),
    
    // 文件相关错误 4xxx
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(4002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超过限制"),
    FILE_NOT_FOUND(4004, "文件不存在"),
    
    // Token相关错误 5xxx
    TOKEN_INVALID(5001, "Token无效"),
    TOKEN_EXPIRED(5002, "Token已过期"),
    TOKEN_MISSING(5003, "Token缺失");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
