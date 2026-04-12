package io.github.mqttplus.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface MqttTemplate {

    void publish(String brokerId, String topic, Object payload);

    void publish(String brokerId, String topic, Object payload, int qos, boolean retained);

    CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload);

    CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload, int qos, boolean retained);

    CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload, Executor executor);

    CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload, int qos, boolean retained, Executor executor);
}