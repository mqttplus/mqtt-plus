package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MqttClientAdapterFactoryRegistry {

    private static final String SPRING_INTEGRATION_ADAPTER_ID = "spring-integration";

    private final Map<String, MqttClientAdapterFactory> factoriesByAdapterId = new LinkedHashMap<>();

    public MqttClientAdapterFactoryRegistry(List<MqttClientAdapterFactory> factories) {
        for (MqttClientAdapterFactory factory : factories) {
            this.factoriesByAdapterId.put(factory.adapterId(), factory);
        }
    }

    public MqttClientAdapterFactory getRequiredByAdapterId(String adapterId) {
        MqttClientAdapterFactory factory = factoriesByAdapterId.get(adapterId);
        if (factory == null) {
            throw new IllegalStateException("No MQTT adapter factory registered for adapter: " + adapterId);
        }
        return factory;
    }

    public MqttClientAdapterFactory resolveFactory(String explicitAdapterId, String mqttVersion) {
        if (explicitAdapterId != null && !explicitAdapterId.isBlank()) {
            MqttClientAdapterFactory explicitFactory = getRequiredByAdapterId(explicitAdapterId);
            if (!explicitFactory.supportsMqttVersion(mqttVersion)) {
                throw new IllegalStateException("MQTT adapter '" + explicitAdapterId + "' does not support version: " + mqttVersion);
            }
            return explicitFactory;
        }

        MqttClientAdapterFactory preferredFactory = factoriesByAdapterId.get(SPRING_INTEGRATION_ADAPTER_ID);
        if (preferredFactory != null && preferredFactory.supportsMqttVersion(mqttVersion)) {
            return preferredFactory;
        }

        for (MqttClientAdapterFactory factory : factoriesByAdapterId.values()) {
            if (factory.supportsMqttVersion(mqttVersion)) {
                return factory;
            }
        }

        throw new IllegalStateException("No MQTT adapter factory registered for version: " + mqttVersion);
    }
}