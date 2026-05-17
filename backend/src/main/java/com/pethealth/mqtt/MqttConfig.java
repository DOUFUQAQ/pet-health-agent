package com.pethealth.mqtt;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置
 */
@Slf4j
@Data
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker-url:${mqtt.broker:tcp://broker.emqx.io:1883}}")
    private String broker;

    @Value("${mqtt.client-id:pet-health-server}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.connection-timeout:${mqtt.timeout:30}}")
    private Integer timeout;

    @Value("${mqtt.keepalive-interval:${mqtt.keepalive:60}}")
    private Integer keepalive;

    @Value("${mqtt.topics.device-data:pet/device/+/data}")
    private String deviceDataTopic;

    @Value("${mqtt.topics.device-status:pet/device/+/status}")
    private String deviceStatusTopic;

    /**
     * 仅构造 MqttClient，不在启动期连接 broker。
     * 真正的 connect/subscribe 由 {@link MqttConnector} 在 ApplicationReadyEvent 后异步重试，
     * 避免公共 broker 抖动时整个应用上下文起不来。
     */
    @Bean
    public MqttClient mqttClient(MqttMessageHandler messageHandler) throws MqttException {
        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
        client.setCallback(messageHandler);
        return client;
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(timeout);
        options.setKeepAliveInterval(keepalive);

        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        return options;
    }

    public String[] getSubscribeTopics() {
        return new String[]{deviceDataTopic, deviceStatusTopic};
    }
}
