# Firmware (ESP32)

This firmware project is built for PlatformIO and follows the backend MQTT contract:

- `pet/device/{deviceCode}/data`
- `pet/device/{deviceCode}/status`

## 1) Quick start

1. Install VSCode + PlatformIO extension.
2. Copy `include/config.example.h` to `include/config.h`.
3. Fill Wi-Fi and MQTT settings in `config.h`.
4. Connect ESP32 and run:
   - Build
   - Upload
   - Monitor

## 2) Current telemetry payloads

- `temperature` (MLX90614)
- `humidity` (DHT22)
- `env_temp` (DHT22)
- `activity` (MPU6050 accel magnitude)
- Status payload includes battery percentage and voltage

## 3) Backend compatibility notes

- Backend handler expects topic format:
  - `pet/device/{deviceCode}/data`
  - `pet/device/{deviceCode}/status`
- Payload must contain `dataType` for data messages.

## 4) Next steps

- Add MAX30102 driver and publish `heart_rate` / `spo2`
- Add servo command subscription from `pet/device/{deviceCode}/control`
- Add deep sleep power mode
