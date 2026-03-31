package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MqttClientAdapterFactoryRegistry {

    private final Map<String, MqttClientAdapterFactory> factories = new LinkedHashMap<>();

    public MqttClientAdapterFactoryRegistry(List<MqttClientAdapterFactory> factories) {
        for (MqttClientAdapterFactory factory : factories) {
            this.factories.put(factory.supportedVersion(), factory);
        }
    }

    public MqttClientAdapterFactory getRequiredFactory(String mqttVersion) {
        MqttClientAdapterFactory factory = factories.get(mqttVersion);
        if (factory == null) {
            throw new IllegalStateException("No MQTT adapter factory registered for version: " + mqttVersion);
        }
        return factory;
    }
}
