package io.github.mqttplus.core.adapter;

import io.github.mqttplus.core.model.MqttBrokerDefinition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public interface MqttClientAdapter {

    String getBrokerId();

    MqttBrokerDefinition getBrokerDefinition();

    void connect();

    void disconnect();

    void subscribe(String topic, int qos);

    void unsubscribe(String topic);

    void publish(String topic, byte[] payload);

    void publish(String topic, byte[] payload, int qos, boolean retained);

    CompletableFuture<Void> publishAsync(String topic, byte[] payload);

    CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained);

    default CompletableFuture<Void> publishAsync(String topic, byte[] payload, Executor executor) {
        return publishAsync(topic, payload, 0, false, executor);
    }

    default CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained,
            Executor executor) {
        return CompletableFuture.completedFuture(null);
    }

    boolean supportsManualAck();

    void addConnectionListener(MqttConnectionListener listener);
}