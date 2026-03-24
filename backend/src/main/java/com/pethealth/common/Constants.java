package com.pethealth.common;

/**
 * 常量类
 */
public class Constants {

    /**
     * 用户角色
     */
    public static class UserRole {
        public static final Integer PET_OWNER = 1;      // 宠物主人
        public static final Integer ADMIN = 2;          // 系统管理员
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final Integer NORMAL = 0;         // 正常
        public static final Integer DISABLED = 1;       // 冻结
    }

    /**
     * 宠物物种
     */
    public static class PetSpecies {
        public static final String DOG = "dog";         // 狗
        public static final String CAT = "cat";         // 猫
    }

    /**
     * 宠物性别
     */
    public static class PetGender {
        public static final Integer FEMALE = 0;         // 母
        public static final Integer MALE = 1;           // 公
    }

    /**
     * 设备类型
     */
    public static class DeviceType {
        public static final String COLLAR = "collar";           // 项圈
        public static final String CAMERA = "camera";           // 摄像头
        public static final String SENSOR = "sensor";           // 传感器
        public static final String FEEDER = "feeder";           // 喂食器
    }

    /**
     * 设备状态
     */
    public static class DeviceStatus {
        public static final Integer OFFLINE = 0;        // 离线
        public static final Integer ONLINE = 1;         // 在线
        public static final Integer FAULT = 2;          // 故障
    }

    /**
     * 数据类型
     */
    public static class DataType {
        public static final String TEMPERATURE = "temperature";     // 体温
        public static final String HEART_RATE = "heart_rate";       // 心率
        public static final String ACTIVITY = "activity";           // 活动量
        public static final String LOCATION = "location";           // 位置
        public static final String ENVIRONMENT = "environment";     // 环境
        public static final String VIDEO = "video";                 // 视频
    }

    /**
     * 预警类型
     */
    public static class AlertType {
        public static final String TEMPERATURE = "temperature";     // 体温异常
        public static final String HEART_RATE = "heart_rate";       // 心率异常
        public static final String ACTIVITY = "activity";           // 活动异常
        public static final String BEHAVIOR = "behavior";           // 行为异常
        public static final String ENVIRONMENT = "environment";     // 环境异常
    }

    /**
     * 预警等级
     */
    public static class AlertLevel {
        public static final Integer INFO = 0;           // 信息
        public static final Integer WARNING = 1;        // 警告
        public static final Integer ERROR = 2;          // 错误
        public static final Integer CRITICAL = 3;       // 严重
    }

    /**
     * 预警状态
     */
    public static class AlertStatus {
        public static final Integer UNREAD = 0;         // 未读
        public static final Integer READ = 1;           // 已读
        public static final Integer HANDLED = 2;        // 已处理
    }

    /**
     * 消息类型
     */
    public static class MessageType {
        public static final String TEXT = "text";       // 文本
        public static final String IMAGE = "image";     // 图片
        public static final String VIDEO = "video";     // 视频
    }

    /**
     * 养护日志类型
     */
    public static class CareLogType {
        public static final String FEEDING = "feeding";         // 喂食
        public static final String MEDICATION = "medication";   // 用药
        public static final String VACCINATION = "vaccination"; // 疫苗
        public static final String CHECKUP = "checkup";         // 体检
        public static final String GROOMING = "grooming";       // 美容
        public static final String OTHER = "other";             // 其他
    }

    /**
     * AI模型
     */
    public static class AIModel {
        public static final String QWEN_MAX = "qwen-max";       // 通义千问文本大模型
        public static final String QWEN_VL_MAX = "qwen-vl-max"; // 通义千问视频大模型
    }

    /**
     * Redis Key前缀
     */
    public static class RedisKey {
        public static final String USER_TOKEN = "user:token:";              // 用户Token
        public static final String DEVICE_DATA = "device:data:";            // 设备数据缓存
        public static final String HEALTH_SCORE = "health:score:";          // 健康评分缓存
        public static final String CHAT_SESSION = "chat:session:";          // 聊天会话缓存
        public static final String ALERT_UNREAD = "alert:unread:";          // 未读预警缓存
    }

    /**
     * MQTT主题前缀
     */
    public static class MqttTopic {
        public static final String DEVICE_DATA = "pet/device/+/data";       // 设备数据上报
        public static final String DEVICE_CONTROL = "pet/device/+/control"; // 设备控制
        public static final String DEVICE_STATUS = "pet/device/+/status";   // 设备状态
    }

    /**
     * 文件上传路径
     */
    public static class FilePath {
        public static final String AVATAR = "avatar/";          // 头像
        public static final String PET_IMAGE = "pet/";          // 宠物图片
        public static final String VIDEO = "video/";            // 视频文件
        public static final String CARE_LOG = "carelog/";       // 养护日志图片
    }

    /**
     * 默认值
     */
    public static class DefaultValue {
        public static final String DEFAULT_AVATAR = "https://example.com/default-avatar.png";
        public static final Integer DEFAULT_HEALTH_SCORE = 85;
        public static final Integer PAGE_SIZE = 20;
        public static final Integer MAX_PAGE_SIZE = 100;
    }
}
