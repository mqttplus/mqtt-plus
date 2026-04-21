package io.github.mqttplus.hivemq;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;

public final class HiveMqMqttClientAdapterFactory implements MqttClientAdapterFactory {

    public static final String ADAPTER_ID = "hivemq";
    public static final String SUPPORTED_MQTT_VERSION = "5.0";

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
        return new HiveMqMqttClientAdapter(brokerDefinition, inboundMessageSink);
    }
}
