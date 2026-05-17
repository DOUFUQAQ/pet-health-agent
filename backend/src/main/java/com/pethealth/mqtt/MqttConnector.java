package com.pethealth.mqtt;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * MQTT 异步连接器：应用启动后台线程持续重试连接 broker，
 * 避免公共 broker 抖动导致 Spring 上下文初始化失败。
 */
@Slf4j
@Component
public class MqttConnector {

    private static final long RETRY_DELAY_MS = 5000L;

    @Resource
    private MqttClient mqttClient;

    @Resource
    private MqttConnectOptions mqttConnectOptions;

    @Resource
    private MqttConfig mqttConfig;

    private volatile boolean shuttingDown = false;
    private Thread worker;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        worker = new Thread(this::connectLoop, "mqtt-connector");
        worker.setDaemon(true);
        worker.start();
    }

    private void connectLoop() {
        while (!shuttingDown && !Thread.currentThread().isInterrupted()) {
            if (mqttClient.isConnected()) {
                return;
            }
            try {
                mqttClient.connect(mqttConnectOptions);
                for (String topic : mqttConfig.getSubscribeTopics()) {
                    mqttClient.subscribe(topic, 1);
                    log.info("MQTT订阅主题: {}", topic);
                }
                log.info("MQTT客户端连接成功");
                return;
            } catch (Exception e) {
                log.warn("MQTT连接失败，{}ms 后重试: {}", RETRY_DELAY_MS, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        shuttingDown = true;
        if (worker != null) {
            worker.interrupt();
        }
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient.close();
        } catch (Exception e) {
            log.warn("MQTT断开异常", e);
        }
    }
}
