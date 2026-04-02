package io.github.mqttplus.integration;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;

public final class SpringIntegrationMqttClientAdapterFactory implements MqttClientAdapterFactory {

    public static final String ADAPTER_ID = "spring-integration";
    public static final String SUPPORTED_MQTT_VERSION = "3.1.1";

    @Override
    public String adapterId() {
        return ADAPTER_ID;
    }

    @Override
    public boolean supportsMqttVersion(String mqttVersion) {
        return SUPPORTED_MQTT_VERSION.equals(mqttVersion);
    }

    @Override
    public MqttClientAdapter create(MqttBrokerDefinition brokerDefinition, MqttInboundMessageSink inboundMessageSink) {
        return new SpringIntegrationMqttClientAdapter(brokerDefinition, inboundMessageSink);
    }
}