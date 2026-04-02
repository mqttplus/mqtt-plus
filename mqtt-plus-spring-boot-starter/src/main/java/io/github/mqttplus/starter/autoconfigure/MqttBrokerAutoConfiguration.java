package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.starter.properties.MqttPlusProperties;

import java.util.Map;

public class MqttBrokerAutoConfiguration {

    public void registerAdapters(MqttPlusProperties properties,
                                 MqttClientAdapterFactoryRegistry factoryRegistry,
                                 MqttClientAdapterRegistry adapterRegistry,
                                 MqttInboundMessageSink inboundMessageSink,
                                 MqttConnectionListener connectionListener) {
        for (Map.Entry<String, MqttPlusProperties.BrokerProperties> entry : properties.getBrokers().entrySet()) {
            String brokerId = entry.getKey();
            MqttPlusProperties.BrokerProperties brokerProperties = entry.getValue();
            MqttBrokerDefinition definition = brokerProperties.toDefinition(brokerId);
            MqttClientAdapterFactory factory = factoryRegistry.resolveFactory(
                    brokerProperties.getAdapter(),
                    brokerProperties.getMqttVersion());
            MqttClientAdapter adapter = factory.create(definition, inboundMessageSink);
            adapter.addConnectionListener(connectionListener);
            adapterRegistry.register(adapter);
            try {
                adapter.connect();
            } catch (RuntimeException ex) {
                adapterRegistry.remove(brokerId);
                throw ex;
            }
        }
    }
}