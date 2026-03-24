-- 智能宠物健康管家系统 - 数据库表结构
-- MySQL 8.0+

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `pet_health` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_health`;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1-宠物主人，2-系统管理员',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-冻结',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  INDEX idx_username (`username`),
  INDEX idx_phone (`phone`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 宠物档案表
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `species` VARCHAR(20) NOT NULL COMMENT '物种：dog-狗，cat-猫',
  `breed` VARCHAR(50) COMMENT '品种',
  `gender` TINYINT COMMENT '性别：0-母，1-公',
  `birthday` DATE COMMENT '出生日期',
  `weight` DECIMAL(5,2) COMMENT '体重（kg）',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `medical_history` TEXT COMMENT '既往病史',
  `allergies` VARCHAR(255) COMMENT '过敏史',
  `vaccination_records` TEXT COMMENT '疫苗接种记录（JSON）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_species (`species`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物档案表';

-- 3. 设备表
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '设备唯一标识',
  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型：collar-项圈，camera-摄像头，sensor-传感器，feeder-喂食器',
  `user_id` BIGINT COMMENT '绑定用户ID',
  `pet_id` BIGINT COMMENT '绑定宠物ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-离线，1-在线，2-故障',
  `battery` INT COMMENT '电量（%）',
  `firmware_version` VARCHAR(20) COMMENT '固件版本',
  `last_online_time` DATETIME COMMENT '最后在线时间',
  `config` JSON COMMENT '设备配置（JSON）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_device_id (`device_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_device_type (`device_type`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 4. 设备数据表（时序数据）
DROP TABLE IF EXISTS `device_data`;
CREATE TABLE `device_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `device_id` VARCHAR(50) NOT NULL COMMENT '设备ID',
  `pet_id` BIGINT COMMENT '宠物ID',
  `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型：temperature-体温，heart_rate-心率，activity-活动量，location-位置，environment-环境，video-视频',
  `data_value` JSON NOT NULL COMMENT '数据值（JSON格式）',
  `timestamp` DATETIME NOT NULL COMMENT '数据时间戳',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX idx_device_id (`device_id`),
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_data_type (`data_type`),
  INDEX idx_timestamp (`timestamp`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备数据表（建议按月分表）';

-- 5. 健康记录表
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `temperature_avg` DECIMAL(4,2) COMMENT '平均体温（°C）',
  `temperature_min` DECIMAL(4,2) COMMENT '最低体温',
  `temperature_max` DECIMAL(4,2) COMMENT '最高体温',
  `heart_rate_avg` INT COMMENT '平均心率（次/分）',
  `heart_rate_min` INT COMMENT '最低心率',
  `heart_rate_max` INT COMMENT '最高心率',
  `activity_steps` INT COMMENT '活动步数',
  `activity_distance` DECIMAL(6,2) COMMENT '活动距离（米）',
  `sleep_duration` INT COMMENT '睡眠时长（分钟）',
  `food_intake` INT COMMENT '进食量（克）',
  `water_intake` INT COMMENT '饮水量（毫升）',
  `abnormal_behaviors` TEXT COMMENT '异常行为记录（JSON）',
  `health_score` INT COMMENT '健康评分（0-100）',
  `ai_analysis` TEXT COMMENT 'AI分析结果',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_pet_date (`pet_id`, `record_date`),
  INDEX idx_record_date (`record_date`),
  INDEX idx_health_score (`health_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录表（每日汇总）';

-- 6. 预警记录表
DROP TABLE IF EXISTS `alert`;
CREATE TABLE `alert` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `alert_type` VARCHAR(30) NOT NULL COMMENT '预警类型：temperature-体温异常，heart_rate-心率异常，activity-活动异常，behavior-行为异常，environment-环境异常',
  `alert_level` TINYINT NOT NULL COMMENT '预警等级：0-信息，1-警告，2-错误，3-严重',
  `alert_title` VARCHAR(100) NOT NULL COMMENT '预警标题',
  `alert_content` TEXT NOT NULL COMMENT '预警内容',
  `alert_data` JSON COMMENT '预警数据（JSON）',
  `suggestion` TEXT COMMENT '处置建议',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未读，1-已读，2-已处理',
  `alert_time` DATETIME NOT NULL COMMENT '预警时间',
  `read_time` DATETIME COMMENT '阅读时间',
  `handle_time` DATETIME COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_alert_type (`alert_type`),
  INDEX idx_alert_level (`alert_level`),
  INDEX idx_status (`status`),
  INDEX idx_alert_time (`alert_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- 7. AI对话历史表
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '对话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT COMMENT '关联宠物ID',
  `session_id` VARCHAR(50) NOT NULL COMMENT '会话ID',
  `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型：text-文本，image-图片，video-视频',
  `user_message` TEXT NOT NULL COMMENT '用户消息',
  `media_url` VARCHAR(255) COMMENT '媒体文件URL',
  `ai_response` TEXT COMMENT 'AI回复',
  `model_used` VARCHAR(50) COMMENT '使用的模型',
  `tokens_used` INT COMMENT '消耗的Token数',
  `response_time` INT COMMENT '响应时间（毫秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_session_id (`session_id`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

-- 8. 养护日志表
DROP TABLE IF EXISTS `care_log`;
CREATE TABLE `care_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `log_type` VARCHAR(20) NOT NULL COMMENT '日志类型：feeding-喂食，medication-用药，vaccination-疫苗，checkup-体检，grooming-美容，other-其他',
  `log_title` VARCHAR(100) NOT NULL COMMENT '日志标题',
  `log_content` TEXT COMMENT '日志内容',
  `log_data` JSON COMMENT '日志数据（JSON）',
  `images` TEXT COMMENT '图片URLs（JSON数组）',
  `log_time` DATETIME NOT NULL COMMENT '日志时间',
  `reminder_time` DATETIME COMMENT '提醒时间（如疫苗到期）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_log_type (`log_type`),
  INDEX idx_log_time (`log_time`),
  INDEX idx_reminder_time (`reminder_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养护日志表';

-- 9. 知识库表（用于AI问答）
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `category` VARCHAR(50) NOT NULL COMMENT '分类：disease-疾病，symptom-症状，treatment-治疗，nutrition-营养，behavior-行为',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `keywords` VARCHAR(255) COMMENT '关键词（逗号分隔）',
  `species` VARCHAR(20) COMMENT '适用物种：dog-狗，cat-猫，all-通用',
  `source` VARCHAR(255) COMMENT '来源',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX idx_category (`category`),
  INDEX idx_species (`species`),
  INDEX idx_status (`status`),
  FULLTEXT INDEX ft_content (`title`, `content`, `keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 10. 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_type` VARCHAR(20) NOT NULL COMMENT '配置类型：string-字符串，number-数字，json-JSON，boolean-布尔',
  `description` VARCHAR(255) COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_config_key (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入初始系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('health.temperature.dog.min', '38.0', 'number', '狗正常体温下限'),
('health.temperature.dog.max', '39.2', 'number', '狗正常体温上限'),
('health.temperature.cat.min', '38.0', 'number', '猫正常体温下限'),
('health.temperature.cat.max', '39.5', 'number', '猫正常体温上限'),
('alert.push.enabled', 'true', 'boolean', '是否启用推送'),
('ai.daily.report.enabled', 'true', 'boolean', '是否启用每日健康报告'),
('ai.daily.report.time', '23:00', 'string', '每日报告生成时间');
