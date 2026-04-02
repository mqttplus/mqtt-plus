package io.github.mqttplus.core.adapter;

import io.github.mqttplus.core.model.MqttBrokerDefinition;

public interface MqttClientAdapterFactory {

    String adapterId();

    boolean supportsMqttVersion(String mqttVersion);

    MqttClientAdapter create(MqttBrokerDefinition brokerDefinition, MqttInboundMessageSink inboundMessageSink);
}