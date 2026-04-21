package io.github.mqttplus.hivemq;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiveMqMqttClientAdapterFactoryTest {

    @Test
    void shouldCreateHiveMqAdapter() {
        HiveMqMqttClientAdapterFactory factory = new HiveMqMqttClientAdapterFactory();
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("127.0.0.1")
                .clientId("hivemq-test")
                .inboundThreadPool(ThreadPoolConfig.builder().build())
                .build();

        MqttClientAdapter adapter = factory.create(definition, (brokerId, topic, payload, headers) -> {
        });

        assertEquals(HiveMqMqttClientAdapterFactory.ADAPTER_ID, factory.adapterId());
        assertTrue(factory.supportsMqttVersion(HiveMqMqttClientAdapterFactory.SUPPORTED_MQTT_VERSION));
        assertInstanceOf(HiveMqMqttClientAdapter.class, adapter);
    }
}
