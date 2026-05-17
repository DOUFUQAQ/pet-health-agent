package com.pethealth.mqtt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.pethealth.service.IDeviceDataService;
import com.pethealth.service.IDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * MQTT消息处理器
 * 主题格式: pet/device/{deviceCode}/data | pet/device/{deviceCode}/status
 */
@Slf4j
@Component
public class MqttMessageHandler implements MqttCallbackExtended {

    @Lazy
    @Resource
    private IDeviceDataService deviceDataService;

    @Lazy
    @Resource
    private IDeviceService deviceService;

    @Lazy
    @Resource
    private ObjectProvider<MqttClient> mqttClientProvider;

    @Lazy
    @Resource
    private ObjectProvider<MqttConfig> mqttConfigProvider;

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (!reconnect) {
            return;
        }
        MqttClient client = mqttClientProvider.getIfAvailable();
        MqttConfig config = mqttConfigProvider.getIfAvailable();
        if (client == null || config == null) {
            return;
        }
        for (String topic : config.getSubscribeTopics()) {
            try {
                client.subscribe(topic, 1);
                log.info("MQTT自动重连后重新订阅主题: {}", topic);
            } catch (Exception e) {
                log.warn("MQTT重连后订阅失败: topic={}, err={}", topic, e.getMessage());
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT连接丢失", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.info("MQTT消息到达: topic={}, payload={}", topic, payload);

            // 解析主题 pet/device/{deviceCode}/{type}
            String[] parts = topic.split("/");
            if (parts.length < 4) {
                log.warn("无效的MQTT主题格式: {}", topic);
                return;
            }

            String deviceCode = parts[2];
            String msgType = parts[3];

            switch (msgType) {
                case "data":
                    handleDataMessage(deviceCode, payload);
                    break;
                case "status":
                    handleStatusMessage(deviceCode, payload);
                    break;
                default:
                    log.warn("未知的MQTT消息类型: {}", msgType);
            }
        } catch (Exception e) {
            log.error("处理MQTT消息异常: topic={}", topic, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 消息投递完成回调
    }

    /**
     * 处理数据上报消息
     * payload示例: {"dataType":"temperature","value":38.5,"unit":"℃","timestamp":1700000000}
     */
    private void handleDataMessage(String deviceCode, String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            String dataType = json.getString("dataType");
            if (dataType == null) {
                log.warn("数据消息缺少dataType字段: {}", payload);
                return;
            }
            deviceDataService.saveDeviceData(deviceCode, dataType, payload);
        } catch (Exception e) {
            log.error("处理数据消息异常: deviceCode={}, payload={}", deviceCode, payload, e);
        }
    }

    /**
     * 处理状态消息
     * payload示例: {"status":1,"battery":85,"firmware":"v1.0.0"}
     */
    private void handleStatusMessage(String deviceCode, String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            Integer status = json.getInteger("status");
            Integer battery = json.getInteger("battery");
            deviceService.updateDeviceStatus(deviceCode, status, battery);
        } catch (Exception e) {
            log.error("处理状态消息异常: deviceCode={}, payload={}", deviceCode, payload, e);
        }
    }
}
