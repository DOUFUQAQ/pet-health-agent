package com.pethealth.mqtt;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

/**
 * MQTT消息发布器
 */
@Slf4j
@Component
public class MqttPublisher {

    @Resource
    private MqttClient mqttClient;

    /**
     * 发布消息
     */
    public void publish(String topic, String payload, int qos) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            mqttClient.publish(topic, message);
            log.info("MQTT消息发布成功: topic={}, payload={}", topic, payload);
        } catch (MqttException e) {
            log.error("MQTT消息发布失败: topic={}, payload={}", topic, payload, e);
        }
    }

    /**
     * 发布消息（默认QoS=1）
     */
    public void publish(String topic, String payload) {
        publish(topic, payload, 1);
    }

    /**
     * 发送设备控制指令
     */
    public void sendDeviceControl(String deviceCode, Object command) {
        String topic = String.format("pet/device/%s/control", deviceCode);
        String payload = JSON.toJSONString(command);
        publish(topic, payload, 1);
    }
}
