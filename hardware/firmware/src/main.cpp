#include <Arduino.h>
#include <WiFi.h>
#include <Wire.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <Adafruit_MLX90614.h>
#include <DHT.h>
#include <MPU6050_tockn.h>
#include <TinyGPSPlus.h>

// 心率传感器（MAX30102）暂时禁用：当前模块在本套供电下持续 power-on reset
// 等主线功能打通 / 换块新模块再启用。打开下面这行即可恢复。
// #define ENABLE_MAX30102

#ifdef ENABLE_MAX30102
#include <MAX30105.h>
#include <heartRate.h>
#endif

#include "config.h"

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);
Adafruit_MLX90614 mlx = Adafruit_MLX90614();
DHT dht22(PIN_DHT22, DHT22);
bool dhtReady = false;
MPU6050 mpu6050(Wire);
TinyGPSPlus gps;
HardwareSerial gpsSerial(2);  // UART2

// ====== 舵机（喂食）控制 ======
// SG90: 50Hz PWM，0.5-2.5ms 高电平对应 0-180°
// 16-bit 分辨率下：0° ≈ 1638, 90° ≈ 4915, 180° ≈ 8192
const int SERVO_LEDC_CHANNEL = 0;
const int SERVO_FREQ_HZ = 50;
const int SERVO_RESOLUTION = 16;
const uint32_t SERVO_DUTY_CLOSED = 1638;   // 0°，料门关闭
const uint32_t SERVO_DUTY_OPEN = 4915;     // 90°，料门打开
unsigned long lastFeedAt = 0;

#ifdef ENABLE_MAX30102
MAX30105 max3010x;
bool maxReady = false;
#endif

unsigned long lastSensorPublishAt = 0;
unsigned long lastStatusPublishAt = 0;

// ====== 计步算法（基于 MPU6050 加速度模长峰值检测）======
// 每 20ms 采样一次加速度模长 → 减重力 → 当 |a|-1g 超过阈值且距上一步 >= MIN_STEP_INTERVAL_MS 时算一步
const float STEP_THRESHOLD_G = 0.18f;            // 加速度波动阈值（除去重力 1g 以外的部分）
const unsigned long MIN_STEP_INTERVAL_MS = 250;  // 每步最小间隔（避免抖动重复计数）
const unsigned long STEP_SAMPLE_INTERVAL_MS = 20;
unsigned long lastStepSampleAt = 0;
unsigned long lastStepAt = 0;
uint32_t stepsSinceLastPublish = 0;
uint32_t stepsTotal = 0;
bool stepActiveSpike = false;                    // 防止单次冲击触发多次计数

#ifdef ENABLE_MAX30102
// 直接读 MAX30102 寄存器 8bit 值（绕过 SparkFun 库，纯 Wire 调用）
uint8_t readMaxRegister(uint8_t reg) {
  Wire.beginTransmission(0x57);
  Wire.write(reg);
  uint8_t err = Wire.endTransmission(false);
  if (err != 0) {
    Serial.printf("  [reg 0x%02X] endTransmission err=%d\n", reg, err);
    return 0xFF;
  }
  uint8_t n = Wire.requestFrom((uint8_t)0x57, (uint8_t)1, (uint8_t)true);
  if (n != 1) {
    Serial.printf("  [reg 0x%02X] requestFrom got %d bytes\n", reg, n);
    return 0xFF;
  }
  return Wire.read();
}

void dumpMaxRegisters() {
  Serial.println("---- MAX30102 寄存器 dump ----");
  Serial.printf("  PART ID    (0xFF) = 0x%02X  (期望: 0x15 MAX30102/05; 0x11=MAX30100)\n", readMaxRegister(0xFF));
  Serial.printf("  REV ID     (0xFE) = 0x%02X\n", readMaxRegister(0xFE));
  Serial.printf("  MODE_CFG   (0x09) = 0x%02X  (期望非0；bit2:0 = 010 心率 / 011 SpO2)\n", readMaxRegister(0x09));
  Serial.printf("  SPO2_CFG   (0x0A) = 0x%02X\n", readMaxRegister(0x0A));
  Serial.printf("  RED_AMP    (0x0C) = 0x%02X  (期望非0；LED 亮度)\n", readMaxRegister(0x0C));
  Serial.printf("  IR_AMP     (0x0D) = 0x%02X  (期望非0；LED 亮度)\n", readMaxRegister(0x0D));
  Serial.printf("  FIFO_WR    (0x04) = 0x%02X\n", readMaxRegister(0x04));
  Serial.printf("  FIFO_RD    (0x06) = 0x%02X\n", readMaxRegister(0x06));
  Serial.printf("  INT_STATUS (0x00) = 0x%02X\n", readMaxRegister(0x00));
  Serial.println("--------------------------------");
}

// 写 MAX30102 寄存器，写后立刻读回校验，失败重试
bool writeMaxRegister(uint8_t reg, uint8_t value, const char* name) {
  for (int attempt = 0; attempt < 5; attempt++) {
    Wire.beginTransmission(0x57);
    Wire.write(reg);
    Wire.write(value);
    if (Wire.endTransmission(true) == 0) {
      delay(2);
      if (readMaxRegister(reg) == value) {
        return true;
      }
    }
    delay(5);
  }
  Serial.printf("  [reg 0x%02X %s] 写入 0x%02X 失败 5 次\n", reg, name, value);
  return false;
}

// 手写 MAX30102 初始化，绕开 SparkFun setup() 的 softReset 时序坑
// LED 电流先小后大：先用小电流写 LED amp，避免 MODE 启动瞬间冲击 VLED 触发 PWR_RDY 重置
bool initMax30102Manual() {
  // 1. Software reset (MODE_CFG bit6=1)，等待自清零
  Wire.beginTransmission(0x57);
  Wire.write(0x09);
  Wire.write(0x40);
  if (Wire.endTransmission(true) != 0) return false;
  for (int i = 0; i < 50; i++) {
    delay(2);
    uint8_t v = readMaxRegister(0x09);
    if (v != 0xFF && (v & 0x40) == 0) break;
  }
  delay(50);  // reset 后再多等一下让内部状态机稳定

  // 2. 先把 LED 电流设小，避免后续 MODE 启动时拉爆 VLED
  // 0x14 ≈ 4mA，足够近距离测心率，远小于之前 0x40+0x24=20mA 的冲击
  writeMaxRegister(0x0C, 0x14, "RED_AMP");
  writeMaxRegister(0x0D, 0x14, "IR_AMP");

  // 3. FIFO 指针清零
  writeMaxRegister(0x04, 0x00, "FIFO_WR_PTR");
  writeMaxRegister(0x05, 0x00, "FIFO_OVF_CNT");
  writeMaxRegister(0x06, 0x00, "FIFO_RD_PTR");

  // 4. FIFO 配置：SMP_AVE=4 (010), ROLLOVER_EN=1, FIFO_A_FULL=15 → 0x4F
  writeMaxRegister(0x08, 0x4F, "FIFO_CFG");

  // 5. SPO2 配置：ADC=4096nA + SR=50Hz (低采样减电流) + LED_PW=411us
  // bits[6:5]=01, bits[4:2]=000, bits[1:0]=11 → 0b00100011 = 0x23
  writeMaxRegister(0x0A, 0x23, "SPO2_CFG");

  // 6. 最后写 MODE，启动测量。Red+IR 双通道（SpO2 模式）→ 0x03
  bool modeOk = writeMaxRegister(0x09, 0x03, "MODE_CFG");

  // 7. 等传感器稳定运行（数个采样周期），再后续 dump 验证
  delay(500);
  return modeOk;
}

// ====== 心率检测：基于 SparkFun heartRate.h ======
const byte HR_RATE_SIZE = 4;            // 滑动平均窗口
byte hrRates[HR_RATE_SIZE];
byte hrRateSpot = 0;
long hrLastBeat = 0;
float hrBeatsPerMinute = 0;
int hrBeatAvg = 0;
unsigned long hrLastSampleAt = 0;       // I2C 节流：每 10ms 才读一次 MAX30102
long hrLastIR = 0;                      // 调试用：最近一次 IR 原始值
#endif  // ENABLE_MAX30102

void scanI2C() {
  Serial.println("Scanning I2C bus...");
  byte found = 0;
  for (byte addr = 1; addr < 127; addr++) {
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() == 0) {
      Serial.printf("  device found at 0x%02X\n", addr);
      found++;
    }
  }
  Serial.printf("Scan done. %d device(s) found.\n", found);
  Serial.println("Expected: 0x5A(MLX90614) 0x57(MAX30102) 0x68(MPU6050)");
}

String buildTopic(const char* suffix) {
  return String(MQTT_DATA_TOPIC_PREFIX) + DEVICE_CODE + String(suffix);
}

// ====== 舵机喂食动作 ======
// 90° 开门 -> 保持 1.5 秒 -> 0° 关门
void runFeedAction() {
  Serial.println("[feed] 舵机开门");
  ledcWrite(SERVO_LEDC_CHANNEL, SERVO_DUTY_OPEN);
  delay(1500);
  ledcWrite(SERVO_LEDC_CHANNEL, SERVO_DUTY_CLOSED);
  Serial.println("[feed] 舵机关门");
  lastFeedAt = millis();
}

// MQTT 控制消息回调：解析 {"command": "feed"} 等指令
void onMqttMessage(char* topic, byte* payload, unsigned int length) {
  Serial.printf("[mqtt] %s len=%u\n", topic, length);
  JsonDocument doc;
  DeserializationError err = deserializeJson(doc, payload, length);
  if (err) {
    Serial.printf("[mqtt] JSON 解析失败: %s\n", err.c_str());
    return;
  }
  const char* command = doc["command"] | "";
  if (strcmp(command, "feed") == 0) {
    runFeedAction();
  } else if (strcmp(command, "restart") == 0) {
    Serial.println("[mqtt] 收到 restart 指令，3 秒后重启");
    delay(3000);
    ESP.restart();
  } else if (strcmp(command, "sync") == 0) {
    Serial.println("[mqtt] 收到 sync 指令，立即上报传感器");
    lastSensorPublishAt = 0;  // 让 loop 立刻触发一次 publishSensors
  } else {
    Serial.printf("[mqtt] 未识别命令: %s\n", command);
  }
}

void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.printf("\nWiFi connected, ip=%s\n", WiFi.localIP().toString().c_str());
}

void connectMQTT() {
  if (mqttClient.connected()) return;
  String clientId = String(DEVICE_CODE) + "-client";
  Serial.print("Connecting MQTT");
  while (!mqttClient.connected()) {
    bool ok = mqttClient.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD);
    if (ok) {
      Serial.println("\nMQTT connected");
      // 连接成功后订阅控制主题
      String controlTopic = String(MQTT_DATA_TOPIC_PREFIX) + DEVICE_CODE + "/control";
      mqttClient.subscribe(controlTopic.c_str(), 1);
      Serial.printf("MQTT subscribed: %s\n", controlTopic.c_str());
    } else {
      Serial.printf("\nMQTT connect failed, rc=%d; retry in 2s\n", mqttClient.state());
      delay(2000);
    }
  }
}

float readBatteryVoltage() {
  int raw = analogRead(PIN_BAT_ADC);
  float adcVoltage = (raw / 4095.0f) * 3.3f;
  return adcVoltage * BAT_ADC_DIVIDER_RATIO;
}

int toBatteryPercent(float voltage) {
  float clamped = voltage;
  if (clamped < 3.0f) clamped = 3.0f;
  if (clamped > 4.2f) clamped = 4.2f;
  return (int)((clamped - 3.0f) * 100.0f / 1.2f);
}

void publishData(const char* dataType, float value, const char* unit) {
  JsonDocument doc;
  doc["dataType"] = dataType;
  doc["value"] = value;
  doc["unit"] = unit;
  doc["timestamp"] = (uint32_t)time(nullptr);

  char payload[256];
  size_t n = serializeJson(doc, payload, sizeof(payload));
  mqttClient.publish(buildTopic(MQTT_DATA_TOPIC_SUFFIX).c_str(), payload, n);
}

void publishStatus() {
  float batV = readBatteryVoltage();
  int batPct = toBatteryPercent(batV);

  JsonDocument doc;
  doc["status"] = 1;
  doc["battery"] = batPct;
  doc["firmware"] = "v0.1.0";
  doc["voltage"] = batV;

  char payload[200];
  size_t n = serializeJson(doc, payload, sizeof(payload));
  mqttClient.publish(buildTopic(MQTT_STATUS_TOPIC_SUFFIX).c_str(), payload, n);
}

void publishSensors() {
  float tempObj = mlx.readObjectTempC();

  float humidity = NAN;
  float envTemp = NAN;
  if (dhtReady) {
    float t = dht22.readTemperature();
    float h = dht22.readHumidity();
    if (!isnan(t) && !isnan(h)) {
      envTemp = t;
      humidity = h;
    }
  }

  mpu6050.update();
  float accMag = sqrt(
      mpu6050.getAccX() * mpu6050.getAccX() +
      mpu6050.getAccY() * mpu6050.getAccY() +
      mpu6050.getAccZ() * mpu6050.getAccZ());

  publishData("temperature", tempObj, "C");
  if (!isnan(humidity)) publishData("humidity", humidity, "%");
  if (!isnan(envTemp)) publishData("env_temp", envTemp, "C");
  publishData("activity", accMag, "g");

  // 步数增量：每 5 秒上报一次自上次 publish 以来新增的步数
  if (stepsSinceLastPublish > 0) {
    publishData("step", (float)stepsSinceLastPublish, "steps");
    stepsSinceLastPublish = 0;
  }

#ifdef ENABLE_MAX30102
  // 心率：仅当采样窗口内确实检出心跳（hrBeatAvg > 30）才上报，避免 0 值污染
  if (maxReady && hrBeatAvg > 30 && hrBeatAvg < 250) {
    publishData("heart_rate", (float)hrBeatAvg, "bpm");
  }
  Serial.printf("[sensors] tempObj=%.2fC envTemp=%.2fC humidity=%.2f%% activity=%.3fg hr=%dbpm ir=%ld steps=%u\n",
                tempObj, envTemp, humidity, accMag, hrBeatAvg, hrLastIR, stepsTotal);
#else
  Serial.printf("[sensors] tempObj=%.2fC envTemp=%.2fC humidity=%.2f%% activity=%.3fg steps=%u\n",
                tempObj, envTemp, humidity, accMag, stepsTotal);
#endif

  if (gps.location.isValid()) {
    double lat = gps.location.lat();
    double lng = gps.location.lng();
    JsonDocument doc;
    doc["dataType"] = "location";
    doc["lat"] = lat;
    doc["lng"] = lng;
    doc["sats"] = gps.satellites.value();
    doc["timestamp"] = (uint32_t)time(nullptr);
    char payload[256];
    size_t n = serializeJson(doc, payload, sizeof(payload));
    mqttClient.publish(buildTopic(MQTT_DATA_TOPIC_SUFFIX).c_str(), payload, n);
    Serial.printf("[gps] lat=%.6f lng=%.6f sats=%d\n", lat, lng, gps.satellites.value());
  } else {
    Serial.printf("[gps] no fix yet, sats=%d, chars=%lu\n",
                  gps.satellites.value(), gps.charsProcessed());
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(PIN_LED_STATUS, OUTPUT);
  pinMode(PIN_BAT_ADC, INPUT);

  Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);
  delay(100);
  scanI2C();
  // ====== DHT22 温湿度（单总线，需要 4.7kΩ 上拉到 VCC）======
  dht22.begin();
  // DHT22 上电后第一次读取约需 2 秒稳定时间
  delay(2000);
  float dhtTest = dht22.readTemperature();
  if (isnan(dhtTest)) {
    dhtReady = false;
    Serial.printf("DHT22 not responding on GPIO%d (检查 4.7k 上拉电阻 / VCC / GND)\n", PIN_DHT22);
  } else {
    dhtReady = true;
    Serial.printf("DHT22 ready on GPIO%d, init temp=%.1fC\n", PIN_DHT22, dhtTest);
  }
  mlx.begin();
  mpu6050.begin();
  mpu6050.calcGyroOffsets(true);

#ifdef ENABLE_MAX30102
  // ====== MAX30102 心率传感器 ======
  // 用标准 100kHz：MLX90614 是 SMBus 设备，I2C_SPEED_FAST(400kHz) 会让它 timeout
  // SparkFun 的 begin() 只校验 Part ID，不写配置——所以即便它返回 true 也得自己 init
  if (max3010x.begin(Wire, I2C_SPEED_STANDARD)) {
    if (initMax30102Manual()) {
      maxReady = true;
      Serial.println("MAX30102 ready (manual init)");
    } else {
      Serial.println("MAX30102 manual init failed");
    }
  } else {
    Serial.println("MAX30102 not found on I2C bus");
  }
  // 保险起见再显式锁回 100kHz，防止任何库内部又改了 clock
  Wire.setClock(100000);
  // 诊断：dump MAX30102 寄存器，确认 setup 是否真生效、模块到底是什么芯片
  dumpMaxRegisters();
#endif

  gpsSerial.begin(GPS_BAUD, SERIAL_8N1, PIN_GPS_RX, PIN_GPS_TX);
  Serial.printf("GPS UART2 started at %d baud (RX=%d TX=%d)\n",
                GPS_BAUD, PIN_GPS_RX, PIN_GPS_TX);

  // ====== 舵机 LEDC PWM 初始化 ======
  ledcSetup(SERVO_LEDC_CHANNEL, SERVO_FREQ_HZ, SERVO_RESOLUTION);
  ledcAttachPin(PIN_SERVO_PWM, SERVO_LEDC_CHANNEL);
  ledcWrite(SERVO_LEDC_CHANNEL, SERVO_DUTY_CLOSED);
  Serial.printf("Servo on GPIO%d, channel=%d, %dHz\n",
                PIN_SERVO_PWM, SERVO_LEDC_CHANNEL, SERVO_FREQ_HZ);

  mqttClient.setServer(MQTT_HOST, MQTT_PORT);
  mqttClient.setCallback(onMqttMessage);

  connectWiFi();
  connectMQTT();
}

void loop() {
  connectWiFi();
  connectMQTT();
  mqttClient.loop();

  while (gpsSerial.available()) {
    gps.encode(gpsSerial.read());
  }

  // ====== 计步采样：每 20ms 检测一次加速度峰值 ======
  if (millis() - lastStepSampleAt >= STEP_SAMPLE_INTERVAL_MS) {
    lastStepSampleAt = millis();
    mpu6050.update();
    float ax = mpu6050.getAccX();
    float ay = mpu6050.getAccY();
    float az = mpu6050.getAccZ();
    float mag = sqrt(ax * ax + ay * ay + az * az);
    float dynamic = fabs(mag - 1.0f);  // 去掉静态 1g 重力分量

    if (dynamic > STEP_THRESHOLD_G) {
      if (!stepActiveSpike && (millis() - lastStepAt) >= MIN_STEP_INTERVAL_MS) {
        stepsSinceLastPublish++;
        stepsTotal++;
        lastStepAt = millis();
        stepActiveSpike = true;
      }
    } else if (dynamic < STEP_THRESHOLD_G * 0.5f) {
      // 回落到阈值一半以下视为本次冲击结束，可以等下一次峰值
      stepActiveSpike = false;
    }
  }

#ifdef ENABLE_MAX30102
  // 心率采样：节流到每 10ms 读一次，避免 I2C 总线竞争 NACK
  // IR > 7000 视为有皮肤接触（SparkFun 官方阈值；低阈值需要紧贴 LED 才会触发）
  if (maxReady && millis() - hrLastSampleAt >= 10) {
    hrLastSampleAt = millis();
    long irValue = max3010x.getIR();
    hrLastIR = irValue;
    if (irValue > 7000 && checkForBeat(irValue)) {
      long delta = millis() - hrLastBeat;
      hrLastBeat = millis();
      hrBeatsPerMinute = 60.0f / (delta / 1000.0f);
      if (hrBeatsPerMinute > 30 && hrBeatsPerMinute < 250) {
        hrRates[hrRateSpot++] = (byte)hrBeatsPerMinute;
        hrRateSpot %= HR_RATE_SIZE;
        int sum = 0;
        for (byte i = 0; i < HR_RATE_SIZE; i++) sum += hrRates[i];
        hrBeatAvg = sum / HR_RATE_SIZE;
      }
    } else if (irValue < 7000) {
      hrBeatAvg = 0;
    }
  }
#endif

  unsigned long now = millis();
  if (now - lastSensorPublishAt >= SENSOR_PUBLISH_INTERVAL_MS) {
    lastSensorPublishAt = now;
    publishSensors();
    digitalWrite(PIN_LED_STATUS, !digitalRead(PIN_LED_STATUS));
  }

  if (now - lastStatusPublishAt >= STATUS_PUBLISH_INTERVAL_MS) {
    lastStatusPublishAt = now;
    publishStatus();
  }
}
