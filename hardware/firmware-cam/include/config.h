#pragma once

// ===========================================================================
// 把下面 3 个凭据 / 地址换成你自己的。这个文件会进版本库，只能放占位符。
// ===========================================================================

// ====== Wi-Fi ======
#define WIFI_SSID     "YOUR_WIFI_SSID"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"

// ====== MQTT ======
#define MQTT_HOST     "broker.emqx.io"
#define MQTT_PORT     1883
#define MQTT_USERNAME ""
#define MQTT_PASSWORD ""
#define DEVICE_CODE   "esp32cam-demo-001"

// 主题前缀必须与后端 MqttMessageHandler 解析逻辑一致
#define MQTT_DATA_TOPIC_PREFIX   "pet/device/"
#define MQTT_DATA_TOPIC_SUFFIX   "/data"
#define MQTT_STATUS_TOPIC_SUFFIX "/status"

// ====== 后端 API ======
// 填运行后端的那台机器的局域网 IP，手机 / ESP32-CAM 要能访问到
#define BACKEND_HOST  "192.168.1.100"
#define BACKEND_PORT  8080
// 后端 base path 为 /api，上传接口为 /upload/image
#define UPLOAD_PATH   "/api/upload/image"

// ====== 摄像头（OV2640，AI Thinker ESP32-CAM 标准引脚）======
#define CAM_PIN_PWDN    32
#define CAM_PIN_RESET   -1
#define CAM_PIN_XCLK     0
#define CAM_PIN_SIOD    26
#define CAM_PIN_SIOC    27
#define CAM_PIN_Y9      35
#define CAM_PIN_Y8      34
#define CAM_PIN_Y7      39
#define CAM_PIN_Y6      36
#define CAM_PIN_Y5      21
#define CAM_PIN_Y4      19
#define CAM_PIN_Y3      18
#define CAM_PIN_Y2       5
#define CAM_PIN_VSYNC   25
#define CAM_PIN_HREF    23
#define CAM_PIN_PCLK    22

#define CAM_FRAME_SIZE    FRAMESIZE_SVGA    // 800x600
#define CAM_JPEG_QUALITY  10                // 0-63，越小画质越好

// ====== LED ======
#define PIN_LED_STATUS   33                 // 板载 LED（低电平点亮）
#define PIN_LED_FLASH     4                 // 闪光灯 LED（高电平点亮）
#define CAM_FLASH_ENABLED 1

// ====== 拍照 / 上报间隔 ======
#define AUTO_CAPTURE_INTERVAL_S  30
#define STATUS_PUBLISH_INTERVAL_MS  30000UL
