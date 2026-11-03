/**
 * ESP32-CAM CameraWebServer
 *
 * 烧录后串口监视器会打印 IP 地址。
 * 小程序摄像头页面通过 http://<IP>/capture 拉取单帧 JPG。
 *
 * WiFi 凭据: 修改下面两个 #define
 */
#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include "esp_camera.h"

// ========== 修改为你的 WiFi ==========
#define WIFI_SSID     "YOUR_WIFI_SSID"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"
// =====================================

WebServer server(80);

// AI Thinker ESP32-CAM 引脚定义
#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27
#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22

bool cameraReady = false;

void setupCamera() {
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size = FRAMESIZE_QVGA;   // 320x240
  config.jpeg_quality = 25;             // 0-63，值越大体积越小速度越快
  config.fb_count = 2;                  // 双缓冲，抓帧和发送可并行
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("摄像头初始化失败: 0x%X\n", err);
    return;
  }

  sensor_t *s = esp_camera_sensor_get();
  if (s) {
    s->set_vflip(s, 1);
    s->set_hmirror(s, 1);
  }
  cameraReady = true;
  Serial.println("摄像头就绪");
}

// ---------- /capture —— 单帧 JPEG（小程序用这个）----------
void handleCapture() {
  if (!cameraReady) {
    server.send(503, "text/plain", "Camera not ready");
    return;
  }
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb) {
    server.send(500, "text/plain", "Capture failed");
    return;
  }
  server.sendHeader("Cache-Control", "no-cache, no-store, must-revalidate");
  server.send_P(200, "image/jpeg", (const char *)fb->buf, fb->len);
  esp_camera_fb_return(fb);
}

// ---------- /stream —— MJPEG 流（浏览器看实时画面）----------
void handleStream() {
  if (!cameraReady) {
    server.send(503, "text/plain", "Camera not ready");
    return;
  }
  WiFiClient client = server.client();
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: multipart/x-mixed-replace; boundary=frame");
  client.println();

  unsigned long start = millis();
  while (client.connected() && (millis() - start < 60000)) {
    camera_fb_t *fb = esp_camera_fb_get();
    if (!fb) { delay(10); continue; }
    client.println("--frame");
    client.printf("Content-Type: image/jpeg\r\nContent-Length: %d\r\n\r\n", fb->len);
    client.write(fb->buf, fb->len);
    client.println();
    esp_camera_fb_return(fb);
  }
}

// ---------- / —— 简单控制页面 ----------
void handleRoot() {
  String html = R"RAW(
<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width">
<title>ESP32-CAM</title>
<style>body{font-family:sans-serif;text-align:center;background:#111;color:#eee}
img{max-width:100%;border:2px solid #333;border-radius:8px}
a{display:inline-block;margin:8px;padding:10px 24px;background:#2563eb;color:#fff;text-decoration:none;border-radius:6px}</style></head>
<body>
<h2>ESP32-CAM</h2>
<img src="/capture" id="cam" style="width:640px;height:480px"><br>
<a href="/capture" target="_blank">单帧截图</a>
<a href="/stream" target="_blank">MJPEG 实时流</a>
<script>setInterval(function(){document.getElementById('cam').src='/capture?_='+Date.now()},500)</script>
</body></html>
)RAW";
  server.send(200, "text/html", html);
}

// ============================================================
void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println("\n===== ESP32-CAM CameraWebServer =====");

  // WiFi
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("连接 WiFi");
  int tries = 0;
  while (WiFi.status() != WL_CONNECTED && tries < 40) {
    delay(500); Serial.print("."); tries++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.printf("\nWiFi 已连接\nIP 地址: %s\n", WiFi.localIP().toString().c_str());
    Serial.printf("小程序填: http://%s/capture\n", WiFi.localIP().toString().c_str());
  } else {
    Serial.println("\nWiFi 连接失败, 继续运行...");
  }

  // 摄像头
  setupCamera();

  // Web 服务器路由
  server.on("/", handleRoot);
  server.on("/capture", handleCapture);
  server.on("/stream", handleStream);
  server.begin();
  Serial.println("HTTP 服务器已启动 (端口 80)");
}

void loop() {
  server.handleClient();
}
