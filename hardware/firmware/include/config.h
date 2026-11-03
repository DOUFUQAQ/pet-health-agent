#pragma once

// ===========================================================================
// 把下面 4 个凭据换成你自己的。这个文件会进版本库，所以只能放占位符。
// ===========================================================================

// ====== Wi-Fi（ESP32 只支持 2.4GHz）======
#define WIFI_SSID     "YOUR_WIFI_SSID"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"

// ====== MQTT ======
// 公网测试可用 broker.emqx.io；接自己的后端时改成后端 EMQX 的 IP / 域名
#define MQTT_HOST     "broker.emqx.io"
#define MQTT_PORT     1883
#define MQTT_USERNAME ""
#define MQTT_PASSWORD ""
#define DEVICE_CODE   "esp32-demo-001"

// 主题前缀必须与后端 MqttMessageHandler 的解析逻辑一致
#define MQTT_DATA_TOPIC_PREFIX   "pet/device/"
#define MQTT_DATA_TOPIC_SUFFIX   "/data"
#define MQTT_STATUS_TOPIC_SUFFIX "/status"

// ====== 采样 / 上报间隔 ======
#define SENSOR_PUBLISH_INTERVAL_MS  5000UL
#define STATUS_PUBLISH_INTERVAL_MS  30000UL

// ====== 引脚（与 hardware/docs 接线文档一致）======
#define PIN_I2C_SDA       21
#define PIN_I2C_SCL       22
#define PIN_SERVO_PWM     18
#define PIN_MAX30102_INT  15
#define PIN_LED_STATUS    4
#define PIN_BAT_ADC       36
#define PIN_DHT22         27   // DHT22 单总线数据脚（替代 I2C 的 AHT20）

// 100k/100k 分压 -> 0.5，电池真实电压 = ADC 电压 * 2
#define BAT_ADC_DIVIDER_RATIO 2.0f

// ====== GPS NEO-6M (UART2) ======
// 接线：GPS TX -> ESP32 GPIO16 (RX2)，GPS RX -> ESP32 GPIO17 (TX2)
#define PIN_GPS_RX     16
#define PIN_GPS_TX     17
#define GPS_BAUD       9600
