package io.github.mqttplus.integration;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringIntegrationMqttClientAdapterFactoryTest {

    @Test
    void shouldCreateSpringIntegrationAdapter() {
        SpringIntegrationMqttClientAdapterFactory factory = new SpringIntegrationMqttClientAdapterFactory();
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("127.0.0.1")
                .clientId("integration-test")
                .inboundThreadPool(ThreadPoolConfig.builder().build())
                .build();

        MqttClientAdapter adapter = factory.create(definition, (brokerId, topic, payload, headers) -> {
        });

        assertEquals(SpringIntegrationMqttClientAdapterFactory.ADAPTER_ID, factory.adapterId());
        assertTrue(factory.supportsMqttVersion(SpringIntegrationMqttClientAdapterFactory.SUPPORTED_MQTT_VERSION));
        assertInstanceOf(SpringIntegrationMqttClientAdapter.class, adapter);
    }
}