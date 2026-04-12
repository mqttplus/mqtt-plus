package io.github.mqttplus.core;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.converter.PayloadSerializer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class DefaultMqttTemplate implements MqttTemplate {

    private final MqttClientAdapterRegistry adapterRegistry;
    private final List<PayloadSerializer> serializers;
    public DefaultMqttTemplate(MqttClientAdapterRegistry adapterRegistry) {
        this(adapterRegistry, List.of());
    }

    public DefaultMqttTemplate(MqttClientAdapterRegistry adapterRegistry,
            List<PayloadSerializer> serializers) {
        this.adapterRegistry = adapterRegistry;
        this.serializers = List.copyOf(serializers);
    }

    @Override
    public void publish(String brokerId, String topic, Object payload) {
        publish(brokerId, topic, payload, 0, false);
    }

    @Override
    public void publish(String brokerId, String topic, Object payload, int qos, boolean retained) {
        MqttClientAdapter adapter = resolveAdapter(brokerId);
        adapter.publish(topic, serializePayload(payload), qos, retained);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload) {
        return publishAsync(brokerId, topic, payload, 0, false);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload, int qos, boolean retained) {
        MqttClientAdapter adapter = resolveAdapter(brokerId);
        return adapter.publishAsync(topic, serializePayload(payload), qos, retained);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload, Executor executor) {
        return publishAsync(brokerId, topic, payload, 0, false, executor);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String brokerId, String topic, Object payload, int qos,
            boolean retained, Executor executor) {
        if (executor == null) throw new NullPointerException();
        MqttClientAdapter adapter = resolveAdapter(brokerId);
        return adapter.publishAsync(topic, serializePayload(payload), qos, retained, executor);
    }

    private MqttClientAdapter resolveAdapter(String brokerId) {
        return adapterRegistry.find(brokerId)
                .orElseThrow(() -> new IllegalArgumentException("No adapter registered for broker: " + brokerId));
    }

    private byte[] serializePayload(Object payload) {
        if (payload == null) {
            return "null".getBytes(StandardCharsets.UTF_8);
        }
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        if (payload instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        for (PayloadSerializer serializer : serializers) {
            if (serializer.supports(payload.getClass())) {
                return serializer.serialize(payload);
            }
        }
        return String.valueOf(payload).getBytes(StandardCharsets.UTF_8);
    }
}
