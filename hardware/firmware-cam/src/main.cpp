#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "esp_camera.h"
#include "config.h"

// ====== 全局状态 ======
WiFiClient mqttWiFiClient;
PubSubClient mqttClient(mqttWiFiClient);
bool cameraReady = false;

unsigned long lastCaptureAt = 0;
unsigned long lastStatusPublishAt = 0;

// ====== 摄像头初始化 ======
bool initCamera() {
    camera_config_t config;
    config.pin_pwdn = CAM_PIN_PWDN;
    config.pin_reset = CAM_PIN_RESET;
    config.pin_xclk = CAM_PIN_XCLK;
    config.pin_sccb_sda = CAM_PIN_SIOD;
    config.pin_sccb_scl = CAM_PIN_SIOC;
    config.pin_d7 = CAM_PIN_Y9;
    config.pin_d6 = CAM_PIN_Y8;
    config.pin_d5 = CAM_PIN_Y7;
    config.pin_d4 = CAM_PIN_Y6;
    config.pin_d3 = CAM_PIN_Y5;
    config.pin_d2 = CAM_PIN_Y4;
    config.pin_d1 = CAM_PIN_Y3;
    config.pin_d0 = CAM_PIN_Y2;
    config.pin_vsync = CAM_PIN_VSYNC;
    config.pin_href = CAM_PIN_HREF;
    config.pin_pclk = CAM_PIN_PCLK;

    config.xclk_freq_hz = 20000000;          // OV2640 标准 XCLK 20MHz
    config.ledc_timer = LEDC_TIMER_0;
    config.ledc_channel = LEDC_CHANNEL_0;
    config.pixel_format = PIXFORMAT_JPEG;
    config.frame_size = CAM_FRAME_SIZE;
    config.jpeg_quality = CAM_JPEG_QUALITY;
    config.fb_count = 1;                     // 单帧缓冲，节省 PSRAM
    config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;

    esp_err_t err = esp_camera_init(&config);
    if (err != ESP_OK) {
        Serial.printf("摄像头初始化失败: 0x%X\n", err);
        return false;
    }

    sensor_t *s = esp_camera_sensor_get();
    if (s) {
        // 根据环境调整：关闭自动白平衡的激进校正，室内偏暖光下表现更好
        s->set_vflip(s, 1);                  // ESP32-CAM 通常倒装，垂直翻转
        s->set_hmirror(s, 1);                // 水平镜像
    }

    Serial.println("OV2640 摄像头初始化成功");
    Serial.printf("  分辨率: %dx%d, JPEG质量: %d\n",
                  config.frame_size == FRAMESIZE_SVGA ? 800 : 0,
                  config.frame_size == FRAMESIZE_SVGA ? 600 : 0,
                  config.jpeg_quality);
    return true;
}

// ====== LED 控制 ======
void setStatusLed(bool on) {
    digitalWrite(PIN_LED_STATUS, on ? LOW : HIGH);  // 板载 LED 低电平点亮
}

void setFlashLed(bool on) {
#if CAM_FLASH_ENABLED
    digitalWrite(PIN_LED_FLASH, on ? HIGH : LOW);
#endif
}

// 快速闪烁 LED 用于错误指示
void blinkError(int times) {
    for (int i = 0; i < times; i++) {
        setStatusLed(true);
        delay(150);
        setStatusLed(false);
        delay(150);
    }
}

// ====== Wi-Fi ======
void connectWiFi() {
    if (WiFi.status() == WL_CONNECTED) return;
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("连接 WiFi");
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 40) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    if (WiFi.status() == WL_CONNECTED) {
        Serial.printf("\nWiFi 已连接, IP=%s, RSSI=%d dBm\n",
                      WiFi.localIP().toString().c_str(), WiFi.RSSI());
    } else {
        Serial.println("\nWiFi 连接超时");
    }
}

// ====== NTP 时间同步 ======
void syncTime() {
    configTime(8 * 3600, 0, "ntp.aliyun.com", "ntp.ntsc.ac.cn", "pool.ntp.org");
    // 等待时间同步（最多等 3 秒）
    int retry = 0;
    time_t now = time(nullptr);
    while (now < 1000000000 && retry < 30) {
        delay(100);
        now = time(nullptr);
        retry++;
    }
    if (now > 1000000000) {
        struct tm timeinfo;
        localtime_r(&now, &timeinfo);
        Serial.printf("NTP 时间同步成功: %04d-%02d-%02d %02d:%02d:%02d\n",
                      timeinfo.tm_year + 1900, timeinfo.tm_mon + 1, timeinfo.tm_mday,
                      timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
    } else {
        Serial.println("NTP 时间同步失败，将使用设备启动时间");
    }
}

// ====== MQTT ======
String buildTopic(const char* suffix) {
    return String(MQTT_DATA_TOPIC_PREFIX) + DEVICE_CODE + String(suffix);
}

void onMqttMessage(char* topic, byte* payload, unsigned int length) {
    Serial.printf("[MQTT] 收到消息: topic=%s, len=%u\n", topic, length);

    JsonDocument doc;
    DeserializationError err = deserializeJson(doc, payload, length);
    if (err) {
        Serial.printf("[MQTT] JSON 解析失败: %s\n", err.c_str());
        return;
    }

    const char* command = doc["command"] | "";
    if (strcmp(command, "capture") == 0) {
        Serial.println("[MQTT] 收到拍照指令");
        lastCaptureAt = 0;  // 触发 loop 中立即拍照
    } else if (strcmp(command, "restart") == 0) {
        Serial.println("[MQTT] 收到重启指令，3秒后重启");
        delay(3000);
        ESP.restart();
    } else {
        Serial.printf("[MQTT] 未识别命令: %s\n", command);
    }
}

void connectMQTT() {
    if (mqttClient.connected()) return;
    String clientId = String(DEVICE_CODE) + "-cam";
    Serial.print("连接 MQTT");
    while (!mqttClient.connected()) {
        bool ok = mqttClient.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD);
        if (ok) {
            Serial.println("\nMQTT 已连接");
            String controlTopic = buildTopic("/control");
            mqttClient.subscribe(controlTopic.c_str(), 1);
            Serial.printf("MQTT 已订阅: %s\n", controlTopic.c_str());
        } else {
            Serial.printf("\nMQTT 连接失败 rc=%d，2秒后重试\n", mqttClient.state());
            delay(2000);
        }
    }
}

// ====== 设备状态上报 ======
void publishStatus() {
    JsonDocument doc;
    doc["status"] = 1;                              // 在线
    doc["firmware"] = "v0.1.0";
    doc["deviceType"] = "camera";
    doc["rssi"] = WiFi.RSSI();
    doc["cameraReady"] = cameraReady;
    doc["ip"] = WiFi.localIP().toString();

    char payload[256];
    String topic = buildTopic(MQTT_STATUS_TOPIC_SUFFIX);
    mqttClient.publish(topic.c_str(), payload, n);
    Serial.printf("[状态] 已上报: %s\n", payload);
}

// ====== 发布图片 URL 到 MQTT 数据主题（供后端消费并触发 AI 分析）======
void publishImageUrl(const char* imageUrl) {
    JsonDocument doc;
    doc["dataType"] = "image";
    doc["url"] = imageUrl;
    doc["deviceCode"] = DEVICE_CODE;
    doc["timestamp"] = (uint32_t)time(nullptr);

    char payload[512];
    size_t n = serializeJson(doc, payload, sizeof(payload));
    String topic = buildTopic(MQTT_DATA_TOPIC_SUFFIX);
    mqttClient.publish(topic.c_str(), payload, n);
    Serial.printf("[数据] 图片 URL 已发布: %s\n", payload);
}

// ====== HTTP 上传图片到后端 ======
// 返回上传后的图片 URL（需调用者 free），失败返回 nullptr
char* uploadPhoto(const uint8_t* jpgData, size_t jpgLen) {
    WiFiClient httpClient;
    httpClient.setTimeout(15);  // 15 秒超时

    Serial.printf("[上传] 连接后端 %s:%d ...\n", BACKEND_HOST, BACKEND_PORT);
    if (!httpClient.connect(BACKEND_HOST, BACKEND_PORT)) {
        Serial.println("[上传] 连接后端失败");
        return nullptr;
    }

    // 构造 multipart/form-data
    const char* boundary = "----PetHealthESP32CAMBoundary";

    char filename[64];
    snprintf(filename, sizeof(filename), "%s-%lu.jpg", DEVICE_CODE, (unsigned long)time(nullptr));

    // 计算 Content-Length
    String headerPart = "--" + String(boundary) + "\r\n";
    headerPart += "Content-Disposition: form-data; name=\"file\"; filename=\"" + String(filename) + "\"\r\n";
    headerPart += "Content-Type: image/jpeg\r\n\r\n";
    String footerPart = "\r\n--" + String(boundary) + "--\r\n";

    size_t contentLength = headerPart.length() + jpgLen + footerPart.length();

    // 发送 HTTP 请求头
    httpClient.printf("POST %s HTTP/1.1\r\n", UPLOAD_PATH);
    httpClient.printf("Host: %s:%d\r\n", BACKEND_HOST, BACKEND_PORT);
    httpClient.printf("Content-Type: multipart/form-data; boundary=%s\r\n", boundary);
    httpClient.printf("Content-Length: %d\r\n", contentLength);
    httpClient.print("Connection: close\r\n\r\n");

    // 发送 multipart body
    httpClient.print(headerPart);
    httpClient.write(jpgData, jpgLen);
    httpClient.print(footerPart);

    Serial.printf("[上传] 已发送 %d 字节 (图片 %d + 头部 %d + 尾部 %d)\n",
                  contentLength, jpgLen, headerPart.length(), footerPart.length());

    // 读取 HTTP 响应头
    int httpCode = -1;
    size_t contentLen = 0;
    while (httpClient.connected()) {
        String line = httpClient.readStringUntil('\n');
        if (line.length() <= 1) break;  // 空行（\r\n 或 \n）
        if (line.startsWith("HTTP/")) {
            httpCode = line.substring(9, 12).toInt();
        }
        if (line.startsWith("Content-Length:")) {
            contentLen = line.substring(15).toInt();
        }
    }

    // 读取响应 body
    String responseBody;
    if (contentLen > 0) {
        char* buf = (char*)malloc(contentLen + 1);
        if (buf) {
            size_t read = 0;
            unsigned long start = millis();
            while (read < contentLen && (millis() - start) < 10000) {
                if (httpClient.available()) {
                    size_t n = httpClient.readBytes(buf + read, contentLen - read);
                    read += n;
                }
            }
            buf[read] = '\0';
            responseBody = String(buf);
            free(buf);
        }
    }

    httpClient.stop();

    Serial.printf("[上传] HTTP %d, 响应长度=%d\n", httpCode, contentLen);
    if (httpCode != 200 || responseBody.length() == 0) {
        Serial.printf("[上传] 失败, body=%s\n", responseBody.c_str());
        return nullptr;
    }

    // 解析响应 JSON，提取 URL
    JsonDocument respDoc;
    DeserializationError err = deserializeJson(respDoc, responseBody);
    if (err) {
        Serial.printf("[上传] JSON 解析失败: %s\nbody=%s\n", err.c_str(), responseBody.c_str());
        return nullptr;
    }

    int code = respDoc["code"] | -1;
    const char* url = respDoc["data"]["url"] | nullptr;
    if (code != 200 || !url) {
        Serial.printf("[上传] 后端返回错误: code=%d, msg=%s\n",
                      code, (const char*)(respDoc["message"] | ""));
        return nullptr;
    }

    Serial.printf("[上传] 成功, URL=%s\n", url);
    return strdup(url);
}

// ====== 拍照并上传 ======
void captureAndUpload() {
    if (!cameraReady) {
        Serial.println("[拍照] 摄像头未就绪，跳过");
        return;
    }

    Serial.println("[拍照] 开始采集图像...");

    // 开闪光灯
    setFlashLed(true);
    delay(50);  // 闪光灯稳定

    camera_fb_t* fb = esp_camera_fb_get();
    if (!fb) {
        Serial.println("[拍照] 获取帧缓冲失败");
        setFlashLed(false);
        blinkError(3);
        return;
    }

    Serial.printf("[拍照] 图像大小: %d 字节\n", fb->len);

    // 拷贝 JPEG 数据，尽快释放帧缓冲
    uint8_t* jpgBuf = (uint8_t*)malloc(fb->len);
    if (!jpgBuf) {
        Serial.println("[拍照] 内存分配失败");
        esp_camera_fb_return(fb);
        setFlashLed(false);
        blinkError(4);
        return;
    }
    memcpy(jpgBuf, fb->buf, fb->len);
    size_t jpgLen = fb->len;
    esp_camera_fb_return(fb);

    // 关闪光灯
    setFlashLed(false);

    // 上传
    setStatusLed(true);  // LED 常亮表示上传中
    char* imageUrl = uploadPhoto(jpgBuf, jpgLen);
    free(jpgBuf);
    setStatusLed(false);

    if (imageUrl) {
        // 通过 MQTT 发布图片 URL，后端可据此触发 AI 分析
        if (mqttClient.connected()) {
            publishImageUrl(imageUrl);
        }
        free(imageUrl);
    } else {
        blinkError(2);
    }
}

// ====== Arduino 入口 ======
void setup() {
    Serial.begin(115200);
    delay(1000);
    Serial.println("\n=================================");
    Serial.println("智能宠物健康管家 - ESP32-CAM");
    Serial.printf("设备编号: %s\n", DEVICE_CODE);
    Serial.println("=================================");

    // LED 引脚
    pinMode(PIN_LED_STATUS, OUTPUT);
    setStatusLed(false);
#if CAM_FLASH_ENABLED
    pinMode(PIN_LED_FLASH, OUTPUT);
    setFlashLed(false);
#endif

    // 1. 连接 WiFi
    connectWiFi();
    if (WiFi.status() != WL_CONNECTED) {
        blinkError(5);
        // 继续执行，WiFi 在 loop 中会重试
    }

    // 2. NTP 时间同步
    if (WiFi.status() == WL_CONNECTED) {
        syncTime();
    }

    // 3. 初始化摄像头
    // 给摄像头供电稳定时间（ESP32-CAM 有时需等电压稳定）
    delay(500);
    cameraReady = initCamera();
    if (!cameraReady) {
        Serial.println("摄像头初始化失败，将跳过拍照功能");
        Serial.println("请检查：1) OV2640 排线连接  2) 供电（需 5V/2A）");
        blinkError(3);
    }

    // 4. 连接 MQTT
    mqttClient.setServer(MQTT_HOST, MQTT_PORT);
    mqttClient.setCallback(onMqttMessage);
    connectMQTT();

    // 首次状态上报
    publishStatus();

    Serial.println("ESP32-CAM 初始化完成");
    Serial.printf("自动拍照间隔: %d 秒\n", AUTO_CAPTURE_INTERVAL_S);
}

void loop() {
    // 维护连接
    connectWiFi();
    connectMQTT();
    mqttClient.loop();

    unsigned long now = millis();

    // 自动拍照
    if (AUTO_CAPTURE_INTERVAL_S > 0 && cameraReady) {
        if (now - lastCaptureAt >= (unsigned long)(AUTO_CAPTURE_INTERVAL_S * 1000UL)) {
            lastCaptureAt = now;
            captureAndUpload();
        }
    }

    // 状态上报
    if (now - lastStatusPublishAt >= STATUS_PUBLISH_INTERVAL_MS) {
        lastStatusPublishAt = now;
        publishStatus();
    }

    // 心跳闪烁（空闲时 LED 每隔 2 秒闪一下）
    static unsigned long lastHeartbeat = 0;
    if (now - lastHeartbeat >= 2000) {
        lastHeartbeat = now;
        setStatusLed(true);
        delay(50);
        setStatusLed(false);
    }
}
